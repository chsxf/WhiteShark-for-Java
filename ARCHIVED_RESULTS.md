# White Shark Test Protocol Archived Results
This file archives previous test runs results, ordered in reverse chronology.

Values represent a single serialization or deserialization run and are expressed in milliseconds.

## August 15th, 2015
This test protocol has been run on August 15th, 2015 on a MacBook Pro mi-2009 (2,53 GHz Intel Core 2 Duo, 8 Go 1067 MHz DDR3) running OS X 10.10.4 and Java 8 SE Update 51.

| Process                                | Minimal | Maximal | Average |
|----------------------------------------|--------:|--------:|--------:|
| WhiteShark serialization               | 0.669   | 0.6876  | 0.67844 |
| Java native serizalization             | 0.0822  | 0.1763  | 0.1134  |
| JSON serialization                     | 0.0924  | 0.1286  | 0.10982 |
| WhiteShark immediate deserialization   | 0.6447  | 0.7331  | 0.68897 |
| WhiteShark progressive deserialization | 0.7074  | 0.7367  | 0.72194 |
| Java native deserialization            | 0.1356  | 0.2265  | 0.17968 |
| JSON deserialization                   | 0.1904  | 0.1327  | 0.1148  |

At this time, WhiteShark Java implementation is about 7 times slower than the other options. Optimizations are planned to reduce that gap.