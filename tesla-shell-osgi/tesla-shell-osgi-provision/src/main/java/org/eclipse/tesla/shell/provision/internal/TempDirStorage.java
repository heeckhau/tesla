package org.eclipse.tesla.shell.provision.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.inject.Named;

import org.eclipse.tesla.shell.provision.Storage;
import org.sonatype.inject.Nullable;
import com.google.inject.Inject;

/**
 * TODO
 *
 * @since 1.0
 */
public class TempDirStorage
    implements Storage
{

    public static final String TEMP_DIR = "org.eclipse.tesla.shell.provision.internal.TempDirStorage.tempDir";

    private TempDir tempDir;

    @Inject
    public TempDirStorage( final TempDir tempDir )
    {
        this.tempDir = tempDir;
    }

    @Override
    public InputStream inputStreamFor( final String path )
    {
        try
        {
            final File fileForPath = new File( tempDir.get(), path );
            return new FileInputStream( fileForPath );
        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( "Could not create input stream for " + path, e );
        }
    }

    @Override
    public OutputStream outputStreamFor( final String path )
    {
        try
        {
            final File fileForPath = new File( tempDir.get(), path );
            if ( !( fileForPath.getParentFile().exists() || fileForPath.getParentFile().mkdirs() ) )
            {
                throw new RuntimeException( "Could not create output stream for " + path );
            }
            return new FileOutputStream( fileForPath );
        }
        catch ( FileNotFoundException e )
        {
            throw new RuntimeException( "Could not create output stream for " + path, e );
        }
    }

    @Override
    public boolean exists( final String path )
    {
        return new File( tempDir.get(), path ).exists();
    }

    public static class TempDir
    {

        private File value;

        @Inject()
        private void set( @Nullable @Named( TEMP_DIR ) File value )
        {
            this.value = value;
        }

        private File get()
        {
            if ( value == null )
            {
                try
                {
                    value = File.createTempFile( TEMP_DIR, null );
                    if ( !( value.delete() ) )
                    {
                        throw new RuntimeException( "Cannot create temporary directory" );
                    }
                    value = new File( value.getParentFile(), TEMP_DIR );
                    value.mkdir();
                }
                catch ( IOException e )
                {
                    throw new RuntimeException( "Cannot create temporary directory", e );
                }
            }
            return value;
        }

    }

}
