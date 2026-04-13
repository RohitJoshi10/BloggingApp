package com.BloggingApp.BloggingApp.services.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface FileServiceInterface {
    Map uploadImage(String path, MultipartFile file) throws IOException;
    InputStream getResources(String path, String fileName) throws FileNotFoundException;
}

/*

Industry Standard ke hisaab se hum ye process follow karenge:

Request: Frontend (jaise Postman ya React) ek Post ID aur ek Image File (Multipart) backend ko bhejega.

File Processing: Backend file ko receive karega, uska naam badlega (unique banane ke liye), aur use server ke ek folder mein save karega.

Database Update: Backend us post ko dhoondega aur uske imageName column mein naya file name store kar dega.

Response: Frontend ko updated Post DTO wapas mil jayega.

Step 1: FileService Interface Banana
Sabse pehle, code ko modular (alaag-alag) rakhne ke liye hum ek nayi service banayenge jo sirf files handle karegi. com.BloggingApp.BloggingApp.services.interfaces package mein ek naya interface banaiye:

Step 2: FileService Implementation
Ab com.BloggingApp.BloggingApp.services package mein iski implementation class banate hain. Yahan hum unique filename generate karne ke liye UUID use karenge taaki agar do users abc.png upload karein, toh wo overwrite na hon.

Step 3: Controller mein Endpoint Banana
Ab hum apne main PostController mein aate hain. Humein FileServiceInterface ko inject karna padega.

Naya Endpoint (Upload Image for Post):
Aapke liye Do Important Kaam:
application.properties Update:
src/main/resources/application.properties mein ye line add kijiye taaki Spring ko pata chale ki images kahan save karni hain:
project.image=images/
(Ye aapke project ke root folder mein images naam ka folder bana dega).

AppConstants Check:
Bhai, jaisa aapne pichli baar kiya tha, ye images/ wali value ko bhi centralize kar sakte hain, par @Value wala tarika industry mein zyada common hai configuration ke liye



NOTE: Aapne FileServiceImpl mein f.mkdir() use kiya hai. Agar aapka path complex ho (jaise uploads/images/2024/), toh mkdir() fail ho sakta hai. Iski jagah f.mkdirs() (with an 's') use karna better hota hai kyunki wo nested folders bhi bana deta hai. Lekin abhi ke liye aapka code ekdum sahi chalega.

Postman mein Test Kaise Karein?
Kyuki ye ek MultipartFile hai, toh test karne ka tarika normal JSON se alag hai:

Method: POST

URL: http://localhost:8080/api/post/image/upload/{postId}

Body Tab: Select karein form-data.

Key: Likhiye image (wahi naam jo @RequestParam mein hai).

Key Type: Key ke right side mein dropdown se File select karein.

Value: Ab aap apne computer se koi bhi image select kar sakte hain.

Send: Hit kijiye aur dekhiye aapke project folder mein images naam ka folder ban gaya hoga aur DB mein post update ho gayi hogi.

Ab Aakhri Step (Image Serving):
Image upload toh ho gayi, lekin frontend ko image dikhane ke liye ek aur GET endpoint chahiye hoga PostController mein jo fileService.getResources() ko use kare. Iske bina user image ka URL access nahi kar payega.
 */