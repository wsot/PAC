PAC
===

PAC is the PDroid Application Chooser - designed as a mod to PDroid to allow configuration of which apps can write to the core.

What is the status of PAC?
--------------------------
I originally threw PAC together in 6 or so hours, and have since fixed a couple of critical bugs. PAC now works (reasonably well), BUT it is not built for efficiency. When doing batches of saving or deleting of settings, it may cause a performance hit on the saving/deleting. This can be resolved by adding caching of authorisation for limited periods (5-10 seconds, probably) to reduce calls to the PackageManager, database etc. 

Why did you build it?
---------------------
There has been a bit of talk now and again about modifying the management of security between PDroid core and PDroid management type apps. Right now, security is maintained by each management-type app declaring the 'android.privacy.WRITE_PRIVACY_SETTINGS' with 'signature' protection. The problem with this is essentially that you cannot have more than one management app installed at the same time, unless they are all signed with the same key. There were some noises about alternatives involving encryption keys, etc, and I was concerned that some of these solutions may mean that a single PDroid core installation would be (almost) irreversibly bound to a specific management app. The implication was that multiple management apps would necessitate fragmentation of PDroid patches and installation - not a good thing. When I was complaining about this potential problem, someone (SecUpwN of XDA) suggested that maybe I should just contribute new security functionality to handle this - that's my paraphrasing, anyway. PAC and the associated PDroid core patches are that 'solution'. 

How does it work?
-----------------
PAC adds functionality to the PDroid core to allow applications to be 'authorised' or 'deauthorised' to change PDroid settings - i.e. to use the saveSetting and deleteSettings functions.  
When an app is authorised, the signatures of the package are recorded in the PDroid core database. Then, when access is attempted, the signatures of the app are checked against signatures recorded in the database. If one or more of the signatures match, then the app is allowed to save/delete. Otherwise, permission is denied to do so.  
A new type of application - an example being the PAC App included in this repository - provides the user interface to select which apps are or are not permitted to access the core. To be permitted to authorise or deauthorise management apps, this 'manager controller' app requires the permission 'android.privacy.MANAGE_PRIVACY_APPLICATIONS'.  
It is still necessary for PDroid manager apps (e.g. PDroid 2.0 App, PDroid Manager) to have the 'android.privacy.WRITE_PRIVACY_SETTINGS' permission, but this SHOULD NOT be declared with signature protection (in contrast to how manager apps currently do it). Indeed, my original intention was to declare the 'android.privacy.WRITE_PRIVACY_SETTINGS' permission in the PAC app with 'normal' protection, so no other app could signature protect that permission. I only haven't done so because that completely breaks compatibility with previous apps using the permission signed. Once again, at some point this will change and any PDroid settings management app will stop working if it uses signature protection on the 'android.privacy.WRITE_PRIVACY_SETTINGS' permission.  

How do I install it?
--------------------
Unfortunately, being a modification to the PDroid core, you currently need to be able to compile your own ROM to use this.    
1. Go to to your source directory WHICH MUST HAVE ALREADY BEEN PATCHED USING THE STANDARD PDROID 2.0 PATCHES. You need to be in root of your android source (you will have folders such as build, frameworks, etc in there)
2. Copy the 'authorize.diff' file from the root of the repos file system to your source directory (which you are in).   
3. Patch the source with the authorize.diff patch: the command 'patch -p1 < authorize.diff' should work.  
4. Build!  
5. Install your shiny new build.  
6. Build and install the PAC app yourself, or install the PAC.apk provided in the root of the repository.  

How do I use it?
----------------
Once you've installed the core changes and the PAC app, simply open the PAC app, check the management application(s) you want to be authorised, and hit the 'save' button. Done. 

I still can't get multiple manager apps working together!
---------------------------------------------------------
Currently, all of the manager apps declare the 'android.privacy.WRITE_PRIVACY_SETTINGS' permission with 'signature' protection. The impact is that all the apps using this permission must be signed by the same key.  
I have included binary patches (which you'll need to apply using bsdiff) to change the protection of the 'android.privacy.WRITE_PRIVACY_SETTINGS' permission from 'signature' to 'normal'. This will allow the PDroid management apps (PDroid 2.0 App and PDroid Manager) to peacefully co-exist. DO NOT USED THE PATCHED VERSIONS OF THESE APPS IF YOU ARE NOT USING PDROID WITH THE PAC CORE PATCHES! IT WILL OPEN A SECURITY HOLE THE SIZE OF THE CHANNEL TUNNEL.  
Simply use bsdiff to apply the patch to the app, which will produce a new APK file. Install the new APK. Configure using PAC.  
* For PDroid 2.0 App 1.51 (by CollegeDev): the patch is 'PDroid2.0_USER_changeperms.bsdiff'. Note that this also changes the signature of the package!  
* For PDroid Manager 0.2.7 (by me, FFU5y): the patch is 'PDroid_Manager_0.2.7_changeperms.bsdiff'.


I will not be posting already-patched versions of CollegeDev's PDroid 2.0 App or PDroid Manager unless PAC makes it into the standard core of PDroid. This is for the safety of users (and because it would be really rude to redistribute a patched version of the PDroid 2.0 App) - this way, it is impossible to 'accidently' install these version of the apps.
  

Additional notes:
-----------------
An app which declares the 'android.privacy.MANAGE_PRIVACY_APPLICATIONS' permission SHOULD declare it as signature protected. This is currently is not enforced by the core, but I may add checks for it so please ensure you do conform to this 'suggestion'. This means having multiple 'controller' apps is difficult, because signatures must match. However, the purpose of the chooser app is so simple that it seems unlikely two chooser apps will be competing with radically different feature sets.

The PDroid core patches add four functions to the PrivacySettingsManager service:
  
    getIsAuthorizedManagerApp(int pid)  
This is an internal function, not accessible from outside of the service. It is used in modifications to the 'saveSettings' and 'deleteSettings' functions to check the permission of the calling app.


    getIsAuthorizedManagerApp(String packageName)  
Checks if the app with the provided packageName is configured to be permitted to save or delete settings.


    authorizeManagerApp(String packageName)  
Allows the app with the provided packagename the ability to save or delete settings


    deauthorizeManagerApp(String packageName)  
Revokes the ability of the app with the provided packagename the ability to save or delete settings