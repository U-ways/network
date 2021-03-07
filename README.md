# Penny Plain DLP

A simple data link protocol that implements framing, error detection, message segmentation and reassembly.

### Usage Example

```
# Run PPD as a Message Sender host with MTU of 20
ppd -S --mtu 20

# Run PPD as a Message Receiver host with MTU of 100
ppd -R --mtu 100
```

### Specification

The data link layer protocol splits long messages into a number of segments. Each message segment is packaged as a
separate frame comprising several fields, as follows:

```
|         | Frame     | Frame | Frame     | Segment | Frame     | Message | Field     |          | Frame     |
| Frame   | delimiter | type  | delimiter | length  | delimiter | segment | delimiter | Checksum | delimiter |
|         | (start)   |       |           |         |           |         |           |          | (end)     |
|---------|-----------|-------|-----------|---------|-----------|---------|-----------|----------|-----------|
| Example |    [      |   F   |     ~     |   05    |     ~     |  hello  |     ~     |    39    |     ]     |
```

The frame's regex can be described as follows:

`\[(f|d)~([0-9]{2})~(.{0,99})~([0-9|a-f]{2})\]`

**Notes:**

- This frame format is designed to be read easily by humans, hence it differs in some ways from a real network protocol.

- The segment length and checksum fields always contain two digits. Segment length values less than 10 have a leading
  zero and checksum values less than 16 have a leading zero (e.g. 0c).

- A segment length of zero (00) means there is no message text and the message segment field is empty
  (i.e. there's nothing between the two field delimiters, which must still be present).

- The segment length must not exceed 99, irrespective of the MTU (see below).

- The message segment can contain any sequence of characters supported by the Java String class, including those
  matching the delimiters ('[', ']', '~') and unprintable control codes.

- Character escaping/stuffing is not required for this protocol. An important element of message handling is that a
  message will not contain either a \n or \r character. This is because the Sender is to be written on the assumption
  that a single line of input contains the complete message to be sent.

- The checksum is calculated from the hexadecimal value of the arithmetic sum of all preceding characters in the frame
  except for the starting frame delimiter (i.e. from the frame type through to the final field delimiter, inclusive).
  Only the last two hexadecimal digits are recorded (e.g. if the decimal arithmetic sum is 1234, the checksum is d2)
  because decimal 1234 is hexadecimal 4d2.

- The total length of the frame including all delimiters must not exceed the MTU (maximum transfer unit). The value of
  the MTU is passed to Sender and Receiver as a command line argument pair in the form
  `--mtu N` where N is a non-negative decimal value.

There is an effective limit to the size of the MTU because there is a limit of 99 characters on the length of the
message segment. If an MTU is specified to the Sender that is larger than the effective limit then this should not be
reported as an error. Instead, simply treat the MTU value is being the same as its effective limit and process the
message normally.

### How To Compile The Project and Run The Protocol

1. In the terminal, navigate to root directory for this project. (i.e. `cd path/to/penny-plain-dlp`)
2. Execute `install` script. (i.e. `./install`)
3. Run jar command to create a new archive. (i.e. `./gradlew jar`)
4. Execute `ppd` script with the right arguments.

- For the sender use: `ppd -S --mtu 20`
- For the receiver use: `ppd -R --mtu 20`

### Command Manual

```
NAME

  ppd - penny plain data link protocol

SYNOPSIS

  ppd -[S|R] --mtu [N]

DESCRIPTION

  send or receive messages using penny plain data link protocol

  -[S|R]
         The type of runner this host is:
           S = Message Sender
           R = Message Receiver

  --mtu [N]
         The maximum transfer unit (MTU)
           A non-negative decimal value.
```

___