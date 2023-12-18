package ysoserial;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;

public class GWTSerializer extends Serializer {	
	public GWTSerializer(Object object) {
		super(object);
	}

	public static void serialize(final Object obj, final OutputStream out, String fieldName) throws IOException {
		final ObjectOutputStream objOut = new ObjectOutputStream(out);
		// GWT serialization begins with a field count (in this case, 1)
		// Followed by a field name represented as a serialized String object
		// ...and then finally the actual serialized object
		objOut.writeInt(1);
		objOut.writeObject(fieldName);
		objOut.writeObject(obj);
	}

}