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

package es.uam.eps.tfg.securedpx;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.os.Environment;

public class CipherActivity {
	
	// Encrypt a file with AES
	public static File encondeFile(File file, String password) {
		// Open file for cipher operations
		FileInputStream fin = null;
		File encriptedFile = null;
		
		try {
			// Create FileInputStream object to read the file
			fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
	
			// Reads up to certain bytes of data from this input
			// stream into an array of bytes.
			fin.read(fileContent);
			
			// Create the encrypted file
			encriptedFile = new File(
                    Environment.getExternalStorageDirectory().getPath() +
                    File.separator + file.getName());
			
			// Write the encrypted data in the new file
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(encriptedFile));
			
			// Generate the AES key and encodeData
			byte[] yourKey = generateKey(password);
			byte[] fileBytes = encodeData(yourKey, fileContent);
	
			// Write in the new file
			bos.write(fileBytes);
			bos.flush();
			bos.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
			return null;
		}
		catch (IOException ioe) {
			System.out.println("Exception while reading the file " + ioe);
			return null;
		}
		catch (Exception e) {
			System.out.println("Exception while ciphering the file " + e);
			return null;
		}
		finally {
			// Close the streams
			try {
				if (fin != null) {
					fin.close();
				}
			}
			catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
				return null;
			}
		}
		return encriptedFile;
	}
	
	// Decode a file with AES
	public static int decodeFile(File file, String password) {
		// Open file for cipher operations
		FileInputStream fin = null;
		
		try {
			// Create FileInputStream object to read the file
			fin = new FileInputStream(file);
			byte fileContent[] = new byte[(int)file.length()];
	
			// Reads up to certain bytes of data from this input
			// stream into an array of bytes.
			fin.read(fileContent);
			
			// Create the decoded file
			File decripted = new File(
                    Environment.getExternalStorageDirectory().getPath() +
                    File.separator + "Download" + File.separator +
                    file.getName());
			
			// Write the decoded data in the new file
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(decripted));
			
			// Generate the AES key and decodeData
			byte[] yourKey = generateKey(password);
			byte[] fileBytes = decodeData(yourKey, fileContent);
	
			// Write in the new file
			bos.write(fileBytes);
			bos.flush();
			bos.close();
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found" + e);
			return -1;
		}
		catch (IOException ioe) {
			System.out.println("Exception while reading the file " + ioe);
			return -1;
		}
		catch (Exception e) {
			System.out.println("Exception while ciphering the file " + e);
			return -1;
		}
		finally {
			// Close the streams
			try {
				if (fin != null) {
					fin.close();
				}
			}
			catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
				return -1;
			}
		}
		return 0;
	}
	
	// Generate a private key for AES cipher
	public static byte[] generateKey(String password) throws Exception {
		byte[] keyStart = password.getBytes("UTF-8");

		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(keyStart);
		kgen.init(256, sr);
		SecretKey skey = kgen.generateKey();
		return skey.getEncoded();
	}

	// Encode data in fileData with AES
	public static byte[] encodeData(byte[] key, byte[] fileData)
			throws Exception {
		// Initialization Vector
		// Required for CBC
		byte[] iv ={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00};
		IvParameterSpec ips = new IvParameterSpec(iv);

		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ips);

		byte[] encrypted = cipher.doFinal(fileData);
		return encrypted;
	}

	// Decode data in fileData with AES
	public static byte[] decodeData(byte[] key, byte[] fileData)
			throws Exception {
		// Initialization Vector
		// Required for CBC
		byte[] iv ={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
				0x00,0x00,0x00,0x00};
		IvParameterSpec ips = new IvParameterSpec(iv);
				
		SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, ips);

		byte[] decrypted = cipher.doFinal(fileData);
		return decrypted;
	}
	
	// Function to encode in MD5
	public static String getMD5(String str) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		byte[] b = md.digest(str.getBytes());

		int size = b.length;
		StringBuilder h = new StringBuilder(size);
		for (int i = 0; i < size; i++) {

			int u = b[i] & 255;

			if (u < 16) {
				h.append("0").append(Integer.toHexString(u));
			} else {
				h.append(Integer.toHexString(u));
			}
		}
		return h.toString();
	}
}

