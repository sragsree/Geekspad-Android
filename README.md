# Geekspad

An android based application for tech geeks to connect, organize meetups and other social events. What makes it really cool is that people can rate other people on their technical skills with reference to their interactions with the person at some tech event or some other means. Additionally it packs in the feature for recruiters to find the right person for a position. Push notifications and messaging are other add-on features built into the app.
On the implementation side the following design patterns are used:

  - Singleton Pattern
  - Factory Method Pattern
  - Observer Pattern
  - Builder Pattern

The application's backend is with Firebase which handles authentication as well.Google Maps API used for location plotting. The current version communicate with SDSU bismarck server as the initial trial targets SDSU students.
### Installation

The application can be run using the apk on any device running android 4.3 and above
For contibuting to the development or to view the source code one has to install Android Studio. The installation is as follows

  - Download Android Studio from https://developer.android.com/studio/index.html
  - Launch the .exe file you downloaded.
  - Follow the setup wizard to install Android Studio and any necessary SDK tools.
  - This application can be run in Emulator or on an actual device running Android 4.4 or above.

### Usage

```
public class ChatActivity extends AppCompatActivity {
    static final String TAG = ChatActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // User login
        if (ParseUser.getCurrentUser() != null) { // start with existing user
            startWithCurrentUser();
        } else { // If not logged in, login as a new anonymous user
            login();
        }
    }
```    

### Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request :D

License
----

**Free Software, Hell Yeah!**