/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Michał Niewrzał (Rogue Wave Software Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.lsp4e.php;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.osgi.framework.Bundle;

public class PHPLanguageServer implements StreamConnectionProvider {

	private static final int CONNECTION_PORT = 29543;

	private StreamConnectionProvider provider;

	public PHPLanguageServer() {
		List<String> commands = new ArrayList<>();
		commands.add("php");
		Bundle bundle = Activator.getContext().getBundle();
		Path workingDir = Path.EMPTY;
		URL serverUrl = bundle.getEntry("/server");
		if (serverUrl == null) {
			// in development mode the server is copied to the target directory
			serverUrl = bundle.getEntry("/target/server");
		}
		if (serverUrl == null) { 
			throw new IllegalStateException ("Language server is not available in bundle "+bundle.getSymbolicName()+".");
		}
		try {
			workingDir = new Path(FileLocator.toFileURL(serverUrl).getPath());
		} catch (IOException e) {
			LanguageServerPlugin.logError(e);
		}
		commands.add(workingDir.append("/bin/php-language-server.php").toOSString());
		
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			commands.add("--tcp=127.0.0.1:" + CONNECTION_PORT);
			provider = new SocketStreamConnectionProvider(commands, workingDir.toOSString(), CONNECTION_PORT);
		} else {
			provider = new ProcessStreamConnectionProvider(commands, workingDir.toOSString());
		}
	}

	@Override
	public void start() throws IOException {
		provider.start();
	}

	@Override
	public InputStream getInputStream() {
		return provider.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return provider.getOutputStream();
	}

	@Override
	public void stop() {
		provider.stop();
	}

}
