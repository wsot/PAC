PAC
===

PAC is the PDroid Application Chooser - designed as a mod to PDroid to allow configuration of which apps can write to the core.

It adds three functions to the PrivacySettingsManager service:
getIsAuthorizedManagerApp(String packageName)
authorizeManagerApp(String packageName)
deauthorizeManagerApp(String packageName)


getIsAuthorizedManagerApp(String packageName)
Checks if the app with the provided packageName is configured to be permitted to save or delete settings.

authorizeManagerApp(String packageName)
Allows the app with the provided packagename the ability to save or delete settings

deauthorizeManagerApp(String packageName)
Revokes the ability of the app with the provided packagename the ability to save or delete settings


The packageName is only used as an identifier: when authorizeManagerApp is called, the signatures of the application are recorded. Then, when access is attempted, those signatures are checked against signatures recorded in the database. If one or more of the signatures match, then the app is allowed to save/delete. Otherwise, permission is denied to do so.

An additional permission is also added: android.privacy.MANAGE_PRIVACY_APPLICATIONS
For an appto authorize or deauthorize manager apps, it must have this permission.
