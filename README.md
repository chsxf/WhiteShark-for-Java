# WhiteShark
WhiteShark is a binary serialization format with a very little overhead and a smaller footprint than JSON or Java native serialization format.

# Supported Languages
As a serialization format, WhiteShark can be used with any programming language.
However, at this time, this repository only provides a Java library.

# Usage
We do not provide a JAR file yet for the library, so you have to import the `com.xhaleera.whiteshark` package into your project.

## Serialization
Serialization is very simple with WhiteShark. Supported types are null, boolean, integers, floating-point numbers, strings, arrays and objects.

By design, serialization of object fields is done on an opt-in basis. In other words, you have to specific explicitly the serializable fields.
This is done through the `@WhiteSharkSerializable` annotation.

### Example
```java
class MySerializableClass {
	public string notSerializedString;

	@WhiteSharkSerializable
	public string serializedString;
}
```

### Serialization of native Java `Map` and `Collection` interfaces
`Map` and `Collection` instances are considered as common objects by WhiteShark. This means only their fields annotated with `WhiteSharkSerializable` will be serialized by default.

In the following example, the `serializedMapInstance` field will be serialized, but not its content.

```java
class MySerializableClass {
	public string notSerializedString;

	@WhiteSharkSerializable
	public string serializedString;

	@WhiteSharkSerializable
	public HashMap<String,Object> serializedMapInstance;
}
```

To serialize the content of a `Map` (limited at this time to `Map<String,?>`) or a `Collection`, add `WhiteSharkSerializableMap` or `WhiteSharkSerializableCollection` annotation to this field.

```java
	...
	@WhiteSharkSerializable
	@WhiteSharkSerializableMap
	public HashMap<String,Object> serializedMapInstance;
}
```

You can also add these annotations at the type level, if your class extends `Map<String,?>` or `Collection`.

```java
@WhiteSharkSerializableCollection
class MyCollection extends Collection<Integer> {
	...
}

class MySerializableClass {
	...
	@WhiteSharkSerializable
	public MyCollection serializedCollection;
}
```

Adding `WhiteSharkSerializableMap` or `WhiteSharkSerializableCollection` annotation to a not eligible field or type has no effect.

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

| First Name | Last Name | Age | Male  | Height |
|------------|-----------|-----|-------|--------|
| Charlotte  | HUMBERT   | 30  | False | 1.8 m  |
| Eric       | BALLET    | 38  | True  | 1.65 m |
| Charles    | SAUVEUR   | 35  | True  | 1.8 m  |
| Carli      | BRUNA     | 26  | False | 1.6 m  |
| William    | MARTIN    | 31  | True  | 1.75 m |
| Marine     | DAVID     | 35  | False | 1.55 m |

Each employee has some additional data. For sake of simplification, all employees have the exact same data.

First, a list of work day categories, implemented as a `Map`.

| Category | Count |
|----------|-------|
| missing  | 0     |
| ill      | 2     |
| vacation | 10    |
| years    | 3     |

Then, a list of meta data, also implement as a `Map`.

| Meta          | Value      |
|---------------|------------|
| Date of birth | 1901-01-01 |
| Entry date    | 1902-01-01 |

And finally, a list of skills, implemented as a `Collection`.

| Skill           |
|-----------------|
| Management      |
| Human Resources |

## Output Size
You can find in the following list the amount of data required to store the serialized stream in each format.

| Format      | Size        | Diff to WhiteShark |
|-------------|-------------|--------------------|
| WhiteShark  | 1,165 bytes | -                  |
| Java native | 1,674 bytes | +43.69%            |
| JSON        | 1,446 bytes | +24.12%            |			

## Performance
Soon

# Format specifications
Soon

# License
See [LICENSE.md](LICENSE.md)
