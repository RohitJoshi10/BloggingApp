package com.BloggingApp.BloggingApp.services;

import com.BloggingApp.BloggingApp.entities.Category;
import com.BloggingApp.BloggingApp.entities.Post;
import com.BloggingApp.BloggingApp.entities.PostMedia;
import com.BloggingApp.BloggingApp.entities.User;
import com.BloggingApp.BloggingApp.exceptions.ApiException;
import com.BloggingApp.BloggingApp.exceptions.ResourceNotFoundException;
import com.BloggingApp.BloggingApp.payloads.PostDTO;
import com.BloggingApp.BloggingApp.payloads.PostResponse;
import com.BloggingApp.BloggingApp.repositories.CategoryRepository;
import com.BloggingApp.BloggingApp.repositories.PostMediaRepository;
import com.BloggingApp.BloggingApp.repositories.PostRepository;
import com.BloggingApp.BloggingApp.repositories.UserRepository;
import com.BloggingApp.BloggingApp.services.interfaces.PostServiceInterface;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostServiceInterface {

    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final Cloudinary cloudinary;
    private final FileServiceImpl fileService;
    private final PostMediaRepository postMediaRepository;


    @Override
    public PostDTO createPost(PostDTO postDTO, Integer userId, Integer categoryId) {
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User","id",userId));
        Category category = categoryRepository.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category","id",categoryId));

        Post post = modelMapper.map(postDTO, Post.class);

        post.setAddedDate(new Date());
        post.setUser(user);
        post.setCategory(category);
        Post savedPost = postRepository.save(post);

        return modelMapper.map(savedPost, PostDTO.class);
    }


    @Override
    public PostDTO updatePost(PostDTO postDTO, Integer postId) {
        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post","id", postId));

        // CHECK: Kya banda owner hai ya Admin?
        validateOwnership(post);

        // Basic content update
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());

        // Media Update: Media update idhr se nhi hoga, Kyuki ab Media table alag hai Media update k liye user uploadPostMedia API call karega.

        Post updatedPost = postRepository.save(post);
        return modelMapper.map(updatedPost, PostDTO.class);
    }

    @Override
    public PostDTO movePostToNewCategory(Integer postId, Integer categoryId){
        // 1. Find the post which you want to move
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // CHECK: Kya banda owner hai ya Admin?
        validateOwnership(post);

        // 2. Fetch the new category in which you want to move your post
        Category newCategory = categoryRepository.findById(categoryId).orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        // 3. Now update the post category
        post.setCategory(newCategory);

        // 4. Save the post
        Post updatePost = postRepository.save(post);
        return modelMapper.map(updatePost, PostDTO.class);
    }


    @Override
    public void deletePost(Integer postId) {
        // 1. Post fetch karo
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        // 2. Ownership validate karo
        validateOwnership(post);

        // 3. Cloudinary Cleanup
        // Cloudinary Cleanup: Saari media files par loop chalao
        if (!post.getMediaFiles().isEmpty()) {
            for (PostMedia media : post.getMediaFiles()) {
                try {
                    String resourceType = "image";
                    String type = (media.getFileType() != null) ? media.getFileType().toLowerCase() : "image";

                    if (type.contains("video")) resourceType = "video";
                    else if (type.contains("pdf") || type.contains("application")) resourceType = "raw";

                    cloudinary.uploader().destroy(media.getPublicId(),
                            ObjectUtils.asMap("resource_type", resourceType));

                } catch (IOException e) {
                    System.err.println("Cloudinary cleanup failed for: " + media.getPublicId());
                    // Hum yahan exception nahi fekenge taaki DB se delete ho sake
                }
            }
        }

        // 4. DB se delete karo
        postRepository.delete(post);
    }



    @Override
    public PostResponse getAllPost(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        // STEP 1: Pageable Object Banana
        // Hum Spring ko bata rahe hain ki humein kaunsa page chahiye (0-indexed)
        // aur ek page par kitna data (size) chahiye.
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // STEP 2: Database se 'Page' fetch karna
        // postRepository.findAll(pageable) sirf Pageable ke instructions follow karta hai.
        // Piche SQL query banti hai: SELECT * FROM posts LIMIT 5 OFFSET 0;
        Page<Post> pagePost = postRepository.findAll(pageable);
        return getPostResponseFromPage(pagePost);
    }


    @Override
    public PostDTO getPostById(Integer postId) {
        Post post = postRepository.findById(postId).orElseThrow(()-> new ResourceNotFoundException("Post", "id", postId));
        PostDTO postDTO = modelMapper.map(post, PostDTO.class);

        // Setting total likes count
        postDTO.setLikesCount(post.getLikes().size());

        // Is LoggedIn User have liked it or not
        postDTO.setLikedByMe(checkIfLiked(post));
        return postDTO;
    }


    @Override
    public PostResponse getPostByCategory(Integer categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Category category = categoryRepository.findById(categoryId).orElseThrow(()-> new ResourceNotFoundException("Category", "id", categoryId));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> pagePost = postRepository.findByCategory(category, pageable);

        return getPostResponseFromPage(pagePost);
    }


    @Override
    public PostResponse getPostByUser(Integer userId, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User", "id", userId));

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> pagePost = postRepository.findByUser(user, pageable);

        return getPostResponseFromPage(pagePost);
    }

    @Override
    public PostResponse searchPosts(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Post> pagePost = postRepository.searchByKeyword(keyword, pageable);
        return getPostResponseFromPage(pagePost);
    }


    @Override
    public PostDTO toggleLike(Integer postId, Integer userId) {

        // Currently logged-in user ki details takki jo user login ni hai wo like kre toh error aa jaye
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Fetch the post
        Post post = postRepository.findById(postId).orElseThrow(()->new ResourceNotFoundException("Post","id", postId));

        // 2. Fetch the user
        User user = userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("User","id", userId));

        // ecurity Check: Kya login wala banda wahi hai jiski userId URL mein hai?
        if(!user.getEmail().equals(currentUserEmail)) throw new ApiException("Unauthorized action! You can only like posts using your own account.");

        // 3. Now check If use have already liked the post
        if(post.getLikes().contains(user))
        {
            // If liked then do unlike
            post.getLikes().remove(user);
        }
        else
        {
            // If not liked
            post.getLikes().add(user);
        }

        // 4. Save the post now
        Post updatedPost = postRepository.save(post);
        PostDTO postDTO = modelMapper.map(updatedPost, PostDTO.class);
        postDTO.setLikesCount(updatedPost.getLikes().size());
        postDTO.setLikedByMe(post.getLikes().contains(user));
        return postDTO;
    }

    private PostResponse getPostResponseFromPage(Page<Post> pagePost) {
        // 1. Current user ka email loop se pehle ek hi baar nikal lo
        String currentUserEmail = "";
        try {
            currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            // Handle gracefully
        }

        // Anonymous user check loop se bahar hi kar lo
        final String finalEmail = (currentUserEmail == null || currentUserEmail.equals("anonymousUser")) ? "" : currentUserEmail;

        // STEP 3: Content (Actual Data) nikaalna
        // pagePost ke paas sirf posts nahi hain, balki metadata bhi hai.
        // .getContent() humein us page ki 5 posts ki 'List' nikaal kar deta hai.
        List<Post> allPosts = pagePost.getContent();

//        // STEP 4: Entity ko DTO mein badalna
//        List<PostDTO> postDTO = allPosts.stream()
//                .map((post) -> modelMapper.map(post, PostDTO.class))
//                .toList();


        // STEP 4: Entity ko DTO mein badalna + Likes Logic set karna
        List<PostDTO> postDTOs = allPosts.stream().map((post) -> {
            // 1. Map entity to DTO
            PostDTO dto = modelMapper.map(post, PostDTO.class);

            // 2. Set total likes count
            dto.setLikesCount(post.getLikes().size());

            // Agar user login hai, toh memory se hi check kar lo (EntityGraph ki wajah se DB hit nahi hoga)
            if (!finalEmail.isEmpty()) {
                dto.setLikedByMe(post.getLikes().stream().anyMatch(user -> user.getEmail().equals(finalEmail)));
            } else {
                dto.setLikedByMe(false);
            }

            return dto;
        }).toList();

        // STEP 5: PostResponse (Packaging)
        // Hum sirf list nahi bhejte, balki frontend ko 'Extras' dete hain
        // taaki wo 'Next' aur 'Previous' button bana sake.
        PostResponse postResponse = new PostResponse();
        postResponse.setContent(postDTOs);
        postResponse.setPageNumber(pagePost.getNumber()); // Current Page kaunsa hai
        postResponse.setPageSize(pagePost.getSize());     // Ek page par kitna data hai
        postResponse.setTotalElements(pagePost.getTotalElements()); // Total posts kitni hain DB mein
        postResponse.setTotalPages(pagePost.getTotalPages()); // Total kitne pages banenge
        postResponse.setLastPage(pagePost.isLast());      // Kya ye aakhri page hai?

        return postResponse;
    }

    // --- HELPER METHOD FOR SECURITY ---
    private void validateOwnership(Post post) {
        // Current logged-in user ki details nikalna
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUsername = "";

        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else {
            currentUsername = principal.toString();
        }

        // 1. Agar login wala banda Post ka owner hai (Email match)
        // 2. Ya phir login wala banda ADMIN hai
        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));

        if (!post.getUser().getEmail().equals(currentUsername) && !isAdmin) {
            // Agar dono nahi hain, toh dhakka maar ke bahar nikalo
            throw new ApiException("Unauthorised! You are not the owner of this post and you are not an Admin.");
        }
    }

    // Helper method to check like status :
    private boolean checkIfLiked(Post post){
        try{
            String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            // Agar user login nahi hai toh Spring Security "anonymousUser" return karta hai
            if (currentUserEmail == null || currentUserEmail.equals("anonymousUser")) {
                return false;
            }
            return post.getLikes().stream().anyMatch(user -> user.getEmail().equals(currentUserEmail));
        }catch (Exception e){
            return false;
        }
    }
}


/*

Ye ek bahut gehra aur achha sawal hai! Iska jawab Security aur Data Integrity mein chhupa hai.

Aapko lag raha hai ki jab PostDTO mein ye fields hain, toh humein manual set karne ki kya zaroorat? Iske 3 bade reasons hain:

1. Data Integrity (Kiska Data Hai?)
Agar aap post.setUser(user) set nahi karenge aur direct DTO par bharosa karenge, toh koi bhi smart user Postman se kisi aur ki userId bhej kar uske naam se post kar sakta hai.

Hum kya kar rahe hain: Hum URL se userId le rahe hain (jo security check ke baad aati hai) aur use database se fetch karke manually set kar rahe hain taaki confirm ho ki post sahi bande se linked hai.

2. Default Values (System-Generated Data)
Kuch cheezein user ko decide nahi karni chahiye, wo system (backend) decide karta hai:

addedDate: Agar user DTO mein 2 saal purani date bhej de, toh aapka data galat ho jayega. Isliye hum current date (new Date()) backend se set karte hain.

imageName: Shuruat mein jab post banti hai, tab image upload nahi hui hoti. Isliye hum ek "default.png" set karte hain. Baad mein jab user image upload karega, tab hum use update karenge.

3. Entity vs DTO Relationship
Aapke Post Entity mein user aur category Objects hain, jabki DTO mein wo sirf fields hain.

ModelMapper sirf wahi map kar paata hai jo "Same Name" aur "Simple Type" ke hote hain.

Jab aap database se User object nikaalte hain (userRepository.findById), tab aapko wo pura object post.setUser(user) mein daalna padta hai taaki Hibernate database mein Foreign Key (user_id) create kar sake.

Kya hota agar hum manually set nahi karte?
Agar aap sirf modelMapper.map(postDTO, Post.class) karte:

addedDate null reh jati (ya user ki bheji hui galat date aa jati).

user_id aur category_id database mein null jate, kyunki ModelMapper ko nahi pata ki kis ID ko kis object se link karna hai.

Aapka save() method error throw karta kyunki database mein ye fields nullable = false ho sakti hain.



Java mein this keyword ka use current class ke instance (object) ko refer karne ke liye kiya jata hai. Aapke code mein this.postRepository likhne ke piche 2 bade reasons hain:

1. Clarity (Ye batane ke liye ki ye variable class ka hai)
Jab aap this.postRepository likhte hain, toh padhne waale developer (ya AI) ko turant samajh aa jata hai ki postRepository koi local variable nahi hai jo method ke andar banaya gaya hai, balki ye Class Level (Field) par define kiya gaya hai.

2. Constructor Injection (Lombok @RequiredArgsConstructor)
Aapne class ke upar @RequiredArgsConstructor lagaya hai. Wo annotation piche se ek aisa constructor banata hai:

Java
public PostServiceImpl(PostRepository postRepository, ModelMapper modelMapper ...) {
    this.postRepository = postRepository;
    this.modelMapper = modelMapper;
    // ...
}
Yahan this.postRepository (class ka field) aur postRepository (constructor ka parameter) ke beech farak karne ke liye this zaroori hota hai. Service methods mein bhi ise use karna ek Good Practice maani jati hai taaki local variables aur class variables mein confusion na ho.

Kya iske bina code chalega?
Haan, bilkul chalega. Agar aap sirf postRepository.save(post) likhenge, tab bhi Spring Boot ise sahi se execute karega kyunki wahan koi aur postRepository naam ka local variable nahi hai.

Professional Tip:
Bade projects mein jahan ek hi method mein 10-15 variables hote hain, wahan this use karne se code ki Readability badh jati hai. Aapko pata hota hai ki:

post -> Local variable hai (jo abhi banaya).

this.postRepository -> Inject kiya hua bean hai.



@Cacheable: Ye Spring ko bolta hai: "Pehle Redis mein dekho, agar wahan data hai toh wahi return kar do. Agar nahi hai, toh method chalao aur result Redis mein 'posts' naam ki bucket mein save kar do."

key structure: Pagination mein humne pageNumber ko key banaya hai. Agar hum sirf "posts" key rakhte, toh Page 1 aur Page 2 ka data mix ho jata. Ab har page ka apna alag cache hoga.

@CacheEvict: Ye sabse zaroori hai. Maano tumne ek post delete ki, par Redis mein abhi bhi purana data hai. Toh user ko deleted post dikhti rahegi. allEntries = true karne se jaise hi koi modification (Create/Update/Delete) hoga, Redis ki purani list saaf ho jayegi aur aglo request par fresh data load hoga.
 */