Secure-Box
==========

Dropbox secure use module for Android
--------------------------------------

    ///////////////////////////////////////////////////////////////////////////
    //                      Your app-specific settings.                      //
    ///////////////////////////////////////////////////////////////////////////

    // Replace this with your app key and secret assigned by Dropbox.
    // Note that this is a really insecure way to do this, and you shouldn't
    // ship code which contains your key & secret in such an obvious way.
    // Obfuscation is good.
    final static private String APP_KEY = "**********";
    final static private String APP_SECRET = "**********";
    final static private String CHOOSER_APP_KEY = "**********";
    
CHANGE "**********" for your real Keys obtained in: https://www.dropbox.com/developers
This APP will need **TWO** different keys, one for the *CHOOSER* and one for the *CORE_API*
