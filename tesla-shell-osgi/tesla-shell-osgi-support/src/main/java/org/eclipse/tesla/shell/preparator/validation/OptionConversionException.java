package org.eclipse.tesla.shell.preparator.validation;

import java.lang.reflect.Type;

import org.apache.karaf.shell.commands.CommandException;
import org.apache.karaf.shell.commands.converter.GenericType;
import org.eclipse.tesla.shell.preparator.CommandDescriptor;
import org.eclipse.tesla.shell.preparator.OptionDescriptor;
import org.fusesource.jansi.Ansi;

/**
 * TODO
 *
 * @since 1.0
 */
public class OptionConversionException
    extends CommandException
{

    public OptionConversionException( final CommandDescriptor command,
                                      final OptionDescriptor option,
                                      final Object value,
                                      final Type expectedType,
                                      final Exception cause )
    {
        super(
            Ansi.ansi()
                .fg( Ansi.Color.RED )
                .a( "Error executing command " )
                .a( command.getScope() )
                .a( ":" )
                .a( Ansi.Attribute.INTENSITY_BOLD )
                .a( command.getName() )
                .a( Ansi.Attribute.INTENSITY_BOLD_OFF )
                .a( ": unable to convert option " )
                .a( Ansi.Attribute.INTENSITY_BOLD )
                .a( option.getName() )
                .a( Ansi.Attribute.INTENSITY_BOLD_OFF )
                .a( " with value '" )
                .a( value )
                .a( "' to type " )
                .a( new GenericType( expectedType ).toString() )
                .fg( Ansi.Color.DEFAULT )
                .toString(),
            String.format(
                "Unable to convert option '%s' with value '%s' to type %s",
                option.getName(), value, new GenericType( expectedType ).toString()
            ),
            cause

        );
    }

}
