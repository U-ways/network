# Penny Plain DLP

A simple data link protocol that implements framing, error detection, message segmentation and reassembly.

### Command Manual

```
NAME
  
  ppd - penny plain data link protocol

SYNOPSIS
  
  ppd -[S|R] --mtu [N]
  ppd -help

DESCRIPTION
  
  send or receive messages using penny plain data link protocol
  
  -[S|R]
         The type of runner this host is:
           S = Message sender
           R = Message receiver

  --mtu [N]
         The maximum transfer unit (MTU)
           A non-negative decimal value.
  
  -help  
         prints this message

USAGE EXAMPLE

  ppd -S --mtu 20  # Run PPD as a sender host with MTU of 20
  ppd -R --mtu 20  # Run PPD as a receiver host with MTU of 20
```

### Usage Example

There are a couple of ways to use this implementation:

#### 1. Simply calling the command with the right argument and providing manual input

```sh
ppd -S --mtu 20 # Run PPD as a sender host with MTU of 20
hello           # => [F~05~hello~39]

ppd -R --mtu 20 # Run PPD as a receiver host with MTU of 20
[F~05~hello~39] # => hello
```

#### 2. Redirect the command standard input to a text file containing the frames/message

```sh
ppd -S --mtu 20 < message.txt # sender encodes message text based on ppd specification 
ppd -R --mtu 20 < frames.txt  # receiver decodes frames text based on ppd specification
```

#### 3. Pipe the sender standard output to the receiver standard input

```sh
ppd -S --mtu 20 | ppd -R --mtu 20  # Sender will pipe the frame to receiver and receiver will decode the frame
hello                              # => hello

# You can also pipe a text file as sender input:
cat message.txt | ppd -S --mtu 20 | ppd -R --mtu 20 
```

#### 4. Communication across network services 

Say we have a network service called `www.ppd.io`, and that server supports our PPD protocol.
The server exposes 2 ports with the following behaviour:

- TCP port `63333`: A PPD encoding sender service with an MTU of 20   
- TCP port `63344`: A PPD decoding receiver service with an MTU of 20

We can then use our host and [netcat](https://en.wikipedia.org/wiki/Netcat) command to emulate active communication between our host, and the PPD server as follows:  

```sh
# Sending a message to www.ppd.io receiver:
echo "hello" | ppd -S --mtu 20 | nc www.ppd.io 63344  # => hello

# Receiving a message from the www.ppd.io sender:
echo "hello" | nc www.ppd.io 63333 | ppd -R --mtu 20  # => hello
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

- A segment length of zero (00) means there is no message text, and the message segment field is empty
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

___