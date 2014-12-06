package com.focusit.agent.loader.jassie;

import com.focusit.agent.bond.AgentManager;
import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.io.IOUtils;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.*;

/**
 * Java agent runtime loader
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class AgentLoader {

	public static void addShutdownHook() {
		AgentManager.addShutodwnHook();
	}

	public static void loadAgent() throws URISyntaxException, IOException {

		InputStream in = AgentLoader.class.getResourceAsStream("/bond.jar");

		File temp = File.createTempFile("bond", ".jar");
		String jarPath = temp.getAbsolutePath();
		temp.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(temp)) {
			IOUtils.copy(in, out);
		}

		AgentManager.agentJar = new JarFile(jarPath);

		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		int p = nameOfRunningVM.indexOf('@');
		String pid = nameOfRunningVM.substring(0, p);

//		try {
//			addURLToSystemClassLoader(new URL("jar:file://" + jarPath + "!/"));
//			moveClassPathoToParent();
//		} catch (IntrospectionException e) {
//			System.err.println("Error add to cp: "+e.getMessage());
//		}

		try {
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(jarPath);
			vm.detach();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void moveClassPathoToParent() throws IntrospectionException {
		URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

		URLClassLoader parent = systemClassLoader;

		ClassLoader temp = systemClassLoader;
		while (temp != null) {
			temp = temp.getParent();
			if (!(temp instanceof URLClassLoader))
				break;
			parent = (URLClassLoader) temp;
		}
		Class<URLClassLoader> classLoaderClass = URLClassLoader.class;

		try {
			Method method = classLoaderClass.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			for (URL url : systemClassLoader.getURLs()) {
				method.invoke(parent, url);
			}
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IntrospectionException("Error when adding url to system ClassLoader ");
		}

	}

	public static void addURLToSystemClassLoader(URL url) throws IntrospectionException {
		URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class<URLClassLoader> classLoaderClass = URLClassLoader.class;

		try {
			Method method = classLoaderClass.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			method.invoke(systemClassLoader, url);
		} catch (Throwable t) {
			t.printStackTrace();
			throw new IntrospectionException("Error when adding url to system ClassLoader ");
		}
	}
	/**
	 * Just test of possibility of loading agent at runtime
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		new AgentLoader().loadAgent();
	}

	private static Manifest getManifestFile(JarFile jarFile)
		throws IOException {
		JarEntry je = jarFile.getJarEntry("META-INF/MANIFEST.MF");
		if (je != null) {
			Enumeration entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				je = (JarEntry) entries.nextElement();
				if ("META-INF/MANIFEST.MF".equalsIgnoreCase(je.getName()))
					break;

				else
					je = null;

			}

		}
		// create the manifest object
		Manifest manifest = new Manifest();
		if (je != null)
			manifest.read(jarFile.getInputStream(je));
		return manifest;
	}

	// given a manifest file and given a jar file, make sure that
// the contents of the manifest file is correct and return a
// map of all the valid entries from the manifest
	private static Map pruneManifest(Manifest manifest, JarFile jarFile)
		throws IOException {
		Map map = manifest.getEntries();
		Iterator elements = map.keySet().iterator();
		while (elements.hasNext()) {
			String element = (String) elements.next();
			if (jarFile.getEntry(element) == null)
				elements.remove();

		}
		return map;

	}

	// make sure that the manifest entries are ready for the signed
// JAR manifest file. if we already have a manifest, then we
// make sure that all the elements are valid. if we do not
// have a manifest, then we create a new signed JAR manifest
// file by adding the appropriate headers
	private static Map createEntries(Manifest manifest, JarFile jarFile)
		throws IOException {
		Map entries = null;
		if (manifest.getEntries().size() > 0)
			entries = pruneManifest(manifest, jarFile);

		else {
// if there are no pre-existing entries in the manifest,
// then we put a few default ones in
			Attributes attributes = manifest.getMainAttributes();
			attributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
			attributes.putValue("Created-By", System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ")");
			entries = manifest.getEntries();

		}
		return entries;

	}

	// a helper function that can take entries from one jar file and
// write it to another jar stream
	private static void writeJarEntry(JarEntry je, JarFile jarFile, JarOutputStream jos)
		throws IOException {
		jos.putNextEntry(je);
		byte[] buffer = new byte[2048];
		int read = 0;
		InputStream is = jarFile.getInputStream(je);
		while ((read = is.read(buffer)) > 0)
			jos.write(buffer, 0, read);
		jos.closeEntry();

	}
/*
	// the actual JAR signing method -- this is the method which
// will be called by those wrapping the JARSigner class
	public void signJarFile( JarFile jarFile, OutputStream outputStream ) throws IOException {

// calculate the necessary files for the signed jAR

// get the manifest out of the jar and verify that
// all the entries in the manifest are correct
		Manifest manifest = getManifestFile( jarFile );
		Map entries = createEntries( manifest, jarFile );

// create the message digest and start updating the
// the attributes in the manifest to contain the SHA1
// digests
//		MessageDigest messageDigest = MessageDigest.getInstance( "SHA1" );
//		updateManifestDigest( manifest, jarFile, messageDigest, entries );

// construct the signature file object and the
// signature block objects
//		SignatureFile signatureFile = createSignatureFile( manifest, messageDigest );
//		SignatureFile.Block block = signatureFile.generateBlock( privateKey, certChain, true );


// start writing out the signed JAR file

// write out the manifest to the output jar stream
		String manifestFileName = "META-INF/MANIFEST.MF";
		JarOutputStream jos = new JarOutputStream( outputStream );
		JarEntry manifestFile = new JarEntry( manifestFileName );
		jos.putNextEntry( manifestFile );
		jos.write( manifestBytes, 0, manifestBytes.length );
		jos.closeEntry();

// write out the signature file -- the signatureFile
// object will name itself appropriately
		String signatureFileName = signatureFile.getMetaName();
		JarEntry signatureFileEntry = new JarEntry( signatureFileName );
		jos.putNextEntry( signatureFileEntry );
		signatureFile.write( jos );
		jos.closeEntry();

// write out the signature block file -- again, the block
// will name itself appropriately
		String signatureBlockName = block.getMetaName();
		JarEntry signatureBlockEntry = new JarEntry( signatureBlockName );
		jos.putNextEntry( signatureBlockEntry );
		block.write( jos );
		jos.closeEntry();

// commit the rest of the original entries in the
// META-INF directory. if any of their names conflict
// with one that we created for the signed JAR file, then
// we simply ignore it
		Enumeration metaEntries = jarFile.entries();
		while( metaEntries.hasMoreElements() ) {
			JarEntry metaEntry = (JarEntry)metaEntries.nextElement();
			if( metaEntry.getName().startsWith( "META-INF" ) &&
				!( manifestFileName.equalsIgnoreCase( metaEntry.getName() ) ||
					signatureFileName.equalsIgnoreCase( metaEntry.getName() ) ||
					signatureBlockName.equalsIgnoreCase( metaEntry.getName() ) ) )
				writeJarEntry( metaEntry, jarFile, jos );

		}

// now write out the rest of the files to the stream
		Enumeration allEntries = jarFile.entries();
		while( allEntries.hasMoreElements() ) {
			JarEntry entry = (JarEntry)allEntries.nextElement();
			if( !entry.getName().startsWith( "META-INF" ) )
				writeJarEntry( entry, jarFile, jos );

		}

// finish the stream that we have been writing to
		jos.flush();
		jos.finish();

// close the JAR file that we have been using
		jarFile.close();

	}
*/
}
