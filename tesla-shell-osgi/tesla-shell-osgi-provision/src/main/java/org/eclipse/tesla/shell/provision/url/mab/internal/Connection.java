package org.eclipse.tesla.shell.provision.url.mab.internal;

import static java.lang.String.format;
import static org.eclipse.tesla.shell.provision.url.mab.internal.Maven2OSGiUtils.getBundleSymbolicName;
import static org.eclipse.tesla.shell.provision.url.mab.internal.Maven2OSGiUtils.getVersion;
import static org.sonatype.sisu.maven.bridge.support.ArtifactRequestBuilder.request;
import static org.sonatype.sisu.maven.bridge.support.CollectRequestBuilder.tree;
import static org.sonatype.sisu.maven.bridge.support.ModelBuildingRequestBuilder.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.model.Model;
import org.eclipse.tesla.shell.provision.PathResolver;
import org.eclipse.tesla.shell.provision.Storage;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
import org.sonatype.sisu.maven.bridge.MavenArtifactResolver;
import org.sonatype.sisu.maven.bridge.MavenDependencyTreeResolver;
import org.sonatype.sisu.maven.bridge.MavenModelResolver;
import aQute.lib.osgi.Builder;

/**
 * TODO
 *
 * @since 1.0
 */
public class Connection
    extends URLConnection
{

    private Storage storage;

    private PathResolver pathResolver;

    private MavenModelResolver modelResolver;

    private MavenArtifactResolver artifactResolver;

    private final MavenDependencyTreeResolver dependencyTreeResolver;

    private static final String RECIPE_COMMENT = "Created by " + Connection.class.getName();

    public Connection( final Storage storage,
                       final PathResolver pathResolver,
                       final MavenModelResolver modelResolver,
                       final MavenArtifactResolver artifactResolver,
                       final MavenDependencyTreeResolver dependencyTreeResolver,
                       final URL url )
    {
        super( url );
        this.storage = storage;
        this.pathResolver = pathResolver;
        this.modelResolver = modelResolver;
        this.artifactResolver = artifactResolver;
        this.dependencyTreeResolver = dependencyTreeResolver;
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
        try
        {
            final Artifact artifact = artifactResolver.resolveArtifact( request().artifact( url.getPath() ) );
            if ( isAlreadyAnOSGiBundle( artifact.getFile() ) )
            {
                return new FileInputStream( artifact.getFile() );
            }

            final Properties recipe = calculateRecipe( artifact, url.getPath() );
            final Artifact pomArtifact = pomArtifactFor( artifact );
            recipe.store( storage.outputStreamFor( pathResolver.pathFor( pomArtifact ) ), RECIPE_COMMENT );

            final Artifact osgiArtifact = osgiArtifactFor( artifact );
            return createOSGiBundle( pathResolver.pathFor( osgiArtifact ), recipe, artifact.getFile() );
        }
        catch ( ArtifactResolutionException e )
        {
            final IOException ioException = new IOException( "Failed to resolver URl " + getURL().toExternalForm() );
            ioException.initCause( e );
            throw ioException;
        }
    }

    private DefaultArtifact pomArtifactFor( final Artifact artifact )
    {
        return new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), "osgi",
                                    artifact.getVersion() );
    }

    private DefaultArtifact osgiArtifactFor( final Artifact artifact )
    {
        return new DefaultArtifact( artifact.getGroupId(), artifact.getArtifactId(), "osgi", artifact.getExtension(),
                                    artifact.getVersion() );
    }

    private Properties calculateRecipe( final Artifact artifact, final String coordinates )
    {
        final Boolean useImportPackage = Boolean.valueOf( System.getProperty(
            getClass().getName() + ".useImportPackage", "true" )
        );
        final Boolean useRequireBundle = Boolean.valueOf( System.getProperty(
            getClass().getName() + ".useRequireBundle", "false" )
        );
        final Properties recipeProperties = new Properties();

        recipeProperties.setProperty( "Bundle-SymbolicName", getBundleSymbolicName(
            artifact.getGroupId(), artifact.getArtifactId(), artifact.getFile() )
        );
        recipeProperties.setProperty( "Bundle-Version", getVersion( artifact.getVersion() ) );
        recipeProperties.setProperty( "Import-Package", useImportPackage ? "*" : "!*" );
        recipeProperties.setProperty( "Export-Package", "*" );
        recipeProperties.setProperty( "DynamicImport-Package", "*" );
        recipeProperties.setProperty( "-nouses", "true" );

        try
        {
            final Model model = modelResolver.resolveModel(
                model().pom( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() )
            );
            if ( model.getName() != null && model.getName().trim().length() > 0 )
            {
                recipeProperties.setProperty( "Bundle-Name", model.getName() );
            }
            if ( model.getDescription() != null && model.getDescription().trim().length() > 0 )
            {
                recipeProperties.setProperty( "Bundle-Description", model.getDescription() );
            }
            // TODO use license, organization, ...

            if ( useRequireBundle )
            {
                final DependencyNode tree = dependencyTreeResolver.resolveDependencyTree(
                    tree().model( model().pom( coordinates ) )
                );
                final List<DependencyNode> children = tree.getChildren();
                if ( children != null )
                {
                    final StringBuilder rb = new StringBuilder();
                    for ( final DependencyNode child : children )
                    {
                        if ( !"test".equals( child.getDependency().getScope() ) )
                        {
                            final Artifact da = artifactResolver.resolveArtifact(
                                request().setArtifact( child.getDependency().getArtifact() )
                            );
                            if ( rb.length() > 0 )
                            {
                                rb.append( ", " );
                            }
                            if ( isAlreadyAnOSGiBundle( da.getFile() ) )
                            {
                                final JarFile jarFile = new JarFile( da.getFile() );
                                final Manifest manifest = jarFile.getManifest();
                                final Attributes mainAttributes = manifest.getMainAttributes();
                                rb.append( mainAttributes.getValue( "Bundle-SymbolicName" ).split( ";" )[0] );
                                rb.append( "; bundle-version=" );
                                rb.append( mainAttributes.getValue( "Bundle-Version" ) );
                                rb.append( "; resolution:=optional" );
                            }
                            else
                            {
                                rb.append( getBundleSymbolicName(
                                    da.getGroupId(), da.getArtifactId(), da.getFile() )
                                );
                                rb.append( "; bundle-version=" );
                                rb.append( getVersion( da.getVersion() ) );
                                rb.append( "; resolution:=optional" );
                            }
                        }
                    }
                    if ( rb.length() > 0 )
                    {
                        recipeProperties.put( "Require-Bundle", rb.toString() );
                    }
                }
            }
        }
        catch ( final Exception ignore )
        {

        }
        return recipeProperties;
    }

    private InputStream createOSGiBundle( final String path,
                                          final Properties recipe,
                                          final File jarFile )
    {
        final Builder builder = new Builder();
        try
        {
            builder.mergeProperties( recipe, true );
            builder.setJar( jarFile );
            builder.mergeManifest( builder.getJar().getManifest() );
            builder.calcManifest();
            builder.getJar().write( storage.outputStreamFor( path ) );
            return storage.inputStreamFor( path );
        }
        catch ( final Exception e )
        {
            throw new RuntimeException( format( "OSGi bundle [%s] not created due to [%s]", path, e.getMessage() ), e );
        }
        finally
        {
            builder.close();
        }
    }

    static boolean isAlreadyAnOSGiBundle( final File file )
    {
        if ( file == null )
        {
            return false;
        }
        try
        {
            final JarFile jarFile = new JarFile( file );
            final Manifest manifest = jarFile.getManifest();
            final Attributes mainAttributes = manifest.getMainAttributes();
            return mainAttributes.getValue( "Bundle-SymbolicName" ) != null;
        }
        catch ( final Exception e )
        {
            return false;
        }
    }

}
