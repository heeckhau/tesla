/**********************************************************************************************************************
 * Copyright (c) 2011 to original author or authors                                                                   *
 * All rights reserved. This program and the accompanying materials                                                   *
 * are made available under the terms of the Eclipse Public License v1.0                                              *
 * which accompanies this distribution, and is available at                                                           *
 *   http://www.eclipse.org/legal/epl-v10.html                                                                        *
 **********************************************************************************************************************/
package org.eclipse.tesla.osgi.provision.url.maor.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.tesla.osgi.provision.url.maor.MavenArtifactObrRepository;

/**
 * TODO
 *
 * @since 1.0
 */
public class Connection
    extends URLConnection
{

    public static final String PROTOCOL = "maor";

    private MavenArtifactObrRepository mavenArtifactObrRepository;

    public Connection( final MavenArtifactObrRepository mavenArtifactObrRepository,
                       final URL url )
    {
        super( url );
        this.mavenArtifactObrRepository = mavenArtifactObrRepository;
    }

    @Override
    public void connect()
        throws IOException
    {
        // ignore
    }

    @Override
    public InputStream getInputStream()
        throws IOException
    {
        mavenArtifactObrRepository.create( url.getPath() );
        return mavenArtifactObrRepository.openStream( url.getPath() );
    }

}
