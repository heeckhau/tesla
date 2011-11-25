package org.eclipse.tesla.shell.core;

import static org.eclipse.tesla.shell.core.internal.PropertiesHelper.loadPropertiesFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.tesla.shell.core.internal.PropertiesHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * TODO
 *
 * @since 1.0
 */
public class Main
{

    protected static final String PROFILE = Main.class.getName() + ".profile";

    protected static final String DEFAULT_PROFILE = "default";

    static final boolean FAIL_IF_NOT_FOUND = true;

    private boolean reset;

    private String profile = DEFAULT_PROFILE;

    public Main( final String[] args )
    {
        try
        {
            parseArguments( args );
        }
        catch ( Exception e )
        {
            System.err.println( e.getMessage() );
            exit( 1 );
        }
    }

    protected void exit( int exitCode )
    {
        System.exit( exitCode );
    }

    public static void main( final String[] args )
        throws Exception
    {
        new Main( args ).boot();
    }

    private void boot()
        throws Exception
    {
        final File etc4tsh = new File( System.getProperty( "shell.home" ), "etc/tsh" );
        final File initialBundles = new File( System.getProperty( "shell.home" ), "lib/shell/bundles" );

        System.setProperty( "logback.configurationFile", new File( etc4tsh, "logback.xml" ).getAbsolutePath() );

        final Properties properties = loadProperties( etc4tsh );

        prepareProfile( properties );

        final Framework framework = loadFramework( properties );
        framework.init();

        provisionFromAssembly( initialBundles, framework.getBundleContext() );

        Thread.currentThread().setContextClassLoader( null );
        framework.start();
        provisionFromStartup( etc4tsh, framework.getBundleContext() );

        while ( true )
        {
            final FrameworkEvent event = framework.waitForStop( 0 );
            if ( event.getType() != FrameworkEvent.STOPPED_UPDATE )
            {
                break;
            }
        }
    }

    private void provisionFromStartup( final File etc, final BundleContext bundleContext )
    {
        try
        {
            File propertiesFile = new File( etc, profile + "/bundles.properties" );
            if ( !propertiesFile.exists() )
            {
                propertiesFile = new File( etc, DEFAULT_PROFILE + "/bundles.properties" );
            }
            final Properties properties = loadPropertiesFile(
                propertiesFile.getParentFile(), propertiesFile.getName(), FAIL_IF_NOT_FOUND
            );
            PropertiesHelper.substituteVariables( properties );

            bundleContext.registerService(
                String.class.getName(),
                Main.class.getName(),
                properties
            );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    private void provisionFromAssembly( final File bundlesDir, final BundleContext bundleContext )
        throws Exception
    {
        final Collection<Bundle> bundles = new ArrayList<Bundle>();

        for ( final File startupBundle : findAssemblyBundles( bundlesDir ) )
        {
            final Bundle bundle = bundleContext.installBundle( startupBundle.toURI().toASCIIString() );
            bundles.add( bundle );
        }

        for ( Bundle bundle : bundles )
        {
            final String fragmentHostHeader = (String) bundle.getHeaders().get( Constants.FRAGMENT_HOST );
            if ( fragmentHostHeader == null || fragmentHostHeader.trim().length() == 0 )
            {
                bundle.start();
            }
        }
    }

    // @TestAccessible
    static File[] findAssemblyBundles( final File startup )
        throws Exception
    {
        if ( startup.exists() && startup.isDirectory() )
        {
            return startup.listFiles( new FilenameFilter()
            {

                @Override
                public boolean accept( final File file, final String name )
                {
                    return name.endsWith( ".jar" );
                }

            } );
        }
        else
        {
            return new File[0];
        }
    }

    private Framework loadFramework( final Properties properties )
        throws Exception
    {
        final ClassLoader classLoader = getClass().getClassLoader();
        BufferedReader br = null;
        try
        {
            br = new BufferedReader( new InputStreamReader(
                classLoader.getResourceAsStream( "META-INF/services/" + FrameworkFactory.class.getName() ), "UTF-8" )
            );
            final String factoryClass = br.readLine();
            final FrameworkFactory factory = (FrameworkFactory) classLoader.loadClass( factoryClass ).newInstance();
            return factory.newFramework( properties );
        }
        finally
        {
            IOUtil.close( br );
        }
    }

    private Properties loadProperties( final File etc )
        throws Exception
    {
        File propertiesFile = new File( etc, profile + "/shell.properties" );
        if ( !propertiesFile.exists() )
        {
            propertiesFile = new File( etc, DEFAULT_PROFILE + "/shell.properties" );
        }
        final Properties properties = loadPropertiesFile(
            propertiesFile.getParentFile(), propertiesFile.getName(), FAIL_IF_NOT_FOUND
        );
        PropertiesHelper.substituteVariables( properties );

        final File profileDir = new File( System.getProperty( "user.home" ), ".m2/tsh/" + profile );
        properties.setProperty( PROFILE, profileDir.getAbsolutePath() );

        final File storageDir = new File( profileDir, "storage" );
        properties.setProperty( Constants.FRAMEWORK_STORAGE, storageDir.getAbsolutePath() );

        return properties;
    }

    private void prepareProfile( final Properties properties )
        throws IOException
    {
        final File profileDir = new File( properties.getProperty( PROFILE ) );
        if ( reset && profileDir.exists() )
        {
            FileUtils.deleteDirectory( profileDir );
        }
        if ( !profileDir.exists() && !profileDir.mkdirs() )
        {
            throw new RuntimeException(
                "Could not create shell profile directory: " + profileDir.getAbsolutePath()
            );
        }

        final File storageDir = new File( properties.getProperty( Constants.FRAMEWORK_STORAGE ) );
        if ( !storageDir.exists() && !storageDir.mkdirs() )
        {
            throw new RuntimeException(
                "Could not create shell storage directory: " + profileDir.getAbsolutePath()
            );
        }
    }

    private void parseArguments( final String[] args )
    {
        if ( args != null && args.length > 0 )
        {
            for ( int i = 0; i < args.length; i++ )
            {
                if ( "--reset".equals( args[i] ) )
                {
                    reset = true;
                }
                else if ( "--profile".equals( args[i] ) )
                {
                    i++;
                    if ( i < args.length )
                    {
                        profile = args[i];
                    }
                    else
                    {
                        throw new IllegalArgumentException( "Profile name must be supplied after --profile" );
                    }
                }
                else
                {
                    throw new IllegalArgumentException( "Argument " + args[i] + " is not supported" );
                }
            }
        }
    }

}
