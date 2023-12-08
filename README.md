![badge](https://flat.badgen.net/badge/MyVPN/Still%20in%20development/red) ![badge](https://flat.badgen.net/badge/License/Free/green)

## My VPN

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
4. Install firebase and setup app (Important).

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

4. **Use Firestore in the Project**:
   - This project uses Firestore to manage server configurations. You can find the code related to Firestore in [this file](https://github.com/doxart/MyVPN/blob/master/firestore-collection-template.json). You need to create Firestore documents like that if you want to change it (collection id, document fields e.g..) you need to change codes inside of app. 

     ![Document template](https://github.com/doxart/MyVPN/blob/master/firestore-document-template.png)   

5. **Enable Remote Config**:
   - In the Firebase Console, navigate to the "Remote Config" section.
   - Click on "Get Started" and follow the on-screen instructions to set up Remote Config for your project.

6. **Add Default Parameters**:
   - To get started, upload [this file](https://github.com/doxart/MyVPN/blob/master/remote_config_ivpn.json) to Remote Config.

7. **Add Google AdMob (Optional)**:
   - Create AdMob interstitial, rewarded and banner ads
   - Create Application class to app.
     ```java
     public class MyApplication extends Application {
     @Override
     public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
      }
     }
     ```

8. **Add Adapty Paywall SDK (Optional)**:
   - Create Adapty account and follow documentation about installing app.
   - Visit the [Adapty](https://docs.adapty.io/docs/what-is-adapty).
   - Create Application class to app.
     ```java
     public class MyApplication extends Application {
     @Override
     public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this);
        Adapty.activate(this, "Insert your Adapty App Key", false);
     }
     }
     ```
   - And add app class in your [AndroidManifest.xml](https://github.com/doxart/MyVPN/blob/master/app/src/main/AndroidManifest.xml).
     ```xml
     <application
          android:name=".MyApplication"
     </application>
     ```

11. **You're All Set!**:
   - With Firebase integration, you can easily manage server configurations and other data for the VPN app.


### Contributions:
We appreciate any contributions to this project, whether it's fixing a bug, improving the user interface, or adding new features.

- **Code Contributions:** You can submit a pull request to add code to the project or fix existing code.
- **Bug Fixes:** You can use issues to report or fix bugs.
- **Documentation:** You can make documentation updates or corrections.
- **New Feature Suggestions:** You can open issues to propose new features for the project.

- Please open an issue before making any code changes and let's discuss your proposal.
- Try to include appropriate tests for all code fixes.
- Submit separate pull requests for code changes or feature additions.
- Communicate with sensitivity and respect and try to help other contributors.

- If you need assistance while contributing, you can send a message to [me](https://github.com/doxart).
- If you have any questions about the project, you can send an email to your project contact email address at [my email address](mailto:gkdnzssmn@doxart.com.tr).

### License:
This project is open-source and licensed under the [MIT License](LICENSE). Feel free to use, modify, and distribute it as per the license terms.

### Contact:
If you have any questions or need assistance, please [open an issue](https://github.com/doxart/MyVPN/issues) or reach out to us via email at gkdnzssmn@doxart.com.tr.

We hope you find this VPN application useful, and thank you for your support!


