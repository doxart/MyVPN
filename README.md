## iVPN

Welcome to my VPN application for Android, an open-source project that aims to provide a seamless and secure VPN experience on your Android device. This project was born out of the need for a reliable VPN solution with a focus on ease of use and privacy.

### Features:
- **User-Friendly Interface**: Our VPN app offers an intuitive user interface that makes it easy for anyone to connect to a VPN server with just a few taps.

- **Enhanced Privacy**: We prioritize your online privacy and security. Our VPN app ensures your internet traffic is encrypted, keeping your data safe from prying eyes.

- **OpenVPN Integration**: This app is built on top of the OpenVPN library, a widely recognized and trusted VPN protocol. OpenVPN offers robust security and compatibility with a wide range of devices and platforms.

- **Remote .ovpn File Management**: We've added a unique feature that allows you to remotely control your .ovpn configuration files. Easily manage and switch between different server configurations without the need to manually import files.

### Why This Project?
Android lacks a comprehensive documentation or sample project for building a VPN application, and this project fills that gap. We believe that open-source development is the way to go, and we encourage contributions from the community to make this project even better.

We're excited to share this project with you and welcome your feedback, bug reports, and contributions. Feel free to dive into the code, contribute to the project, or simply use it for your private VPN needs.

### Getting Started:
1. Clone this repository.
2. Build the project using Android Studio.
3. Install the app on your Android device.
4. Connect to a VPN server and enjoy a secure browsing experience.

### Getting Started with Firebase Integration:

To enhance the capabilities of this VPN app, we have integrated Firebase, a comprehensive mobile and web application development platform, for analytics, crash reporting, and more. Here's how to set up Firebase for this project:

1. **Create a Firebase Project**:
   - Visit the [Firebase Console](https://console.firebase.google.com/).
   - Click on "Add Project" and follow the on-screen instructions to create a new Firebase project.

2. **Add Your App to Firebase**:
   - After creating the project, click on "Add app" to add your Android app to Firebase.
   - Follow the setup instructions and download the `google-services.json` file.

3. **Configure Firestore**:
   - In the Firebase Console, navigate to the "Firestore Database" section.
   - Create a new Firestore database or use an existing one.

4. **Security Rules**:
   - Configure security rules to ensure data privacy and security. Here's an example of basic rules:
     ```json
     rules_version = '2';
     service cloud.firestore {
       match /databases/{database}/documents {
         match /{document=**} {
           allow read, write: true;
         }
       }
     }
     ```

5. **Initialize Firestore**:
   - In your Android application code, make sure to initialize Firestore as follows:
     ```java
     import com.google.firebase.firestore.FirebaseFirestore;

     // ...

     // Initialize Firestore
     FirebaseFirestore db = FirebaseFirestore.getInstance();
     ```

6. **Use Firestore in the Project**:
   - This project uses Firestore to manage server configurations. You can find the code related to Firestore in [this file](https://github.com/doxart/iVPN/blob/master/firestore-collection-template.json).

7. **You're All Set!**:
   - With Firestore integration, you can easily manage server configurations and other data for the VPN app.


### Contributions:
We appreciate any contributions to this project, whether it's fixing a bug, improving the user interface, or adding new features. Please check our [Contribution Guidelines](CONTRIBUTING.md) for more information on how to get involved.

### License:
This project is open-source and licensed under the [MIT License](LICENSE.md). Feel free to use, modify, and distribute it as per the license terms.

### Contact:
If you have any questions or need assistance, please [open an issue](https://github.com/doxart/iVPN/issues) or reach out to us via email at [gkdnzssmn@doxart.com.tr].

We hope you find this VPN application useful, and thank you for your support!
