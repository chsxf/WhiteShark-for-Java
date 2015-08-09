# WhiteShark
WhiteShark is a binary serialization format with a very little overhead and a much smaller footprint than JSON or Java native serialization format.

# Supported Languages
As a serialization format, WhiteShark can be used with any programming language.
However, at this time, this repository only provides a Java library.

# Usage
We do not provide a JAR file yet for the library, so you have to import the `com.xhaleera.whiteshark` package into your project.

## Serialization
Serialization is very simple with WhiteShark. Supported types are null, boolean, integers, floating-point numbers, strings, arrays and objects.

By design, serialization of object fields is done on an opt-in basis. In other words, you have to specific explicitly the serializaable fields.
This is done through the `@WhiteSharkSerializable` annotation.

### Example
```java
class MySerializableClass {
	public string notSerializedString;

	@WhiteSharkSerializable
	public string serializedString;
}
```

### Calling the Default Serializer
A WhiteShark stream of serailized data starts with a header. This header contains a custom 4-byte long alphanumeric identifier that indicates the potential stream usage. It allows you during deserialization to ensure the data you're receiving is the right one, and acting accordingly if not.

To serialize an object with default options, just call `WhiteSharkSerializer.serialize()` with the correct arguments.

```java
FileOutputStream fileStream = new FileOutputStream(new File(path));
string streamId = "STRG";
string toSerialize = "It's not very useful to serialize me, but... you know...";
WhiteSharkSerializer.serialize(streamId, fileStream, toSerialize);
fileStream.close();
```

### Calling the Serializer with Options
An alternative version to `WhiteSharkSerializer.serialize()` allows you to pass some options to the serializer.

At this time, only one option is supported:
* **`WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS`**: If set, serialized objects won't include class information and, thus, won't be mapped to their original class on deserialization (not set by default)

```java
FileOutputStream fileStream = new FileOutputStream(new File(path));
string streamID = "STRG";
string toSerialize = "It's not very useful to serialize me, but... you know...";
WhiteSharkSerializer.serialize(streamID, fileStream, toSerialize, WhiteSharkConstants.OPTIONS_OBJECTS_AS_GENERICS);
fileStream.close();
```

*Disabling class mapping, i.e. serializing objects as generics, can also be achieved on a class-by-class basis thanks to the `@WhiteSharkAsGenerics` class annotation.*

## Immediate Deserialization
Immediate deserialization is the simplest and fastest method as it can deserialize your WhiteShark stream in just one call. However, it requires the WhiteShark stream to be fully available during deserialization.

To proceed, just call `WhiteSharkImmediateDeserializer.deserialize()` then pass you stream identifier and input stream.

```java
FileInputStream inStream = new FileInputStream(new File(path));
string streamId
Object o = WhiteSharkImmediateDeserializer.deserialize(streamId, inStream);
inStream.close();
```

## Progressive Deserialization
Progressive deserialization is the method of choice if you need to deserialize your WhiteShark stream *on the flow*. For example, it applies to network communications, if your serialized data is chunked of if you can not or do not want to buffer your whole stream before deserialization occurs.

The following example exhibits how to deserialize a file in several steps:

```java
FileInputStream inStream = new FileInputStream(new File(path));
WhiteSharkProgressiveDeserializer.DeserializationResult result = null;
WhiteSharkProgressiveDeserializer deserializer = new WhiteSharkProgressiveDeserializer(streamId);
byte[] b = new byte[1024];
int c;
while (inStream.available() > b.length) {
	c = inStream.read(b);
	result = deserializer.update(b, 0, c);
	if (result.complete)
		break;
}
if (result != null && !result.complete) {
	c = inStream.read(b);
	result = deserializer.finalize(b, 0, Math.max(0, c));
}
inStream.close();
```

# Comparison with Other Serialization Formats
As a Java library, it is interesting to compare it against the Java native serialization API. It is interesting to compare also against the well-known and widely used JSON serialization format.

## Data Set
As an example, we serialize a list of fictitious employees.

| First Name | Last Name | Age | Male  |
|------------|-----------|-----|-------|
| Charlotte  | HUMBERT   | 30  | False |
| Eric       | BALLET    | 38  | True  |
| Charles    | SAUVEUR   | 35  | True  |
| Carli      | BRUNA     | 26  | False |
| William    | MARTIN    | 31  | True  |
| Marine     | DAVID     | 35  | False |

## Output Size
You can find in the following list the amount of data required to store the serialized stream in each format.

| Format      | Size      | Diff to WhiteShark |
|-------------|-----------|--------------------|
| WhiteShark  | 302 bytes | -                  |
| Java native | 351 bytes | +16.22%            |
| JSON        | 382 bytes | +26.49%            |			

## Performance
Soon

# Format specifications
Soon

# License
See [LICENSE.md](LICENSE.md)
