Secure-Box
==========

Dropbox secure use module for Android
--------------------------------------

This proyect allows the use of the Dropbox platform with an extra layer of security which will encrypt the files uploaded to this site before really uploading them. In order to use it, you will need to change the APP-keys:

In **DBRoulette.java** file:

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
This APP will need **TWO** different keys, one for the *CHOOSER* and one for the *CORE_API*.

After that your APP will be ready to use.

Note that this is an educational proyect, is never going to be used for commercial user. The Dropbox logo and images are property of Dropbox Inc. There is a license available for this proyect and it's included below.

    /*
     * Copyright 2013-14 Ignacio del Pozo Martínez
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *     http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
     
Also this proyect may not be finished without the pieces of code obtained from Anders Kalør and Dropbox Inc, each one
compiled with the corresponding licenses.

This is a still growing proyect so any contribution will be much appreciated. Feel free to contact me on
sak.naxete@gmail.com or contribute with bug fixes or new funcitonality and I will try to answer you/include them as
soon as posible.
