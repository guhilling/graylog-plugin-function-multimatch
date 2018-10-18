[![Build Status](https://travis-ci.org/guhilling/graylog-plugin-function-multimatch.svg?branch=master)](https://travis-ci.org/guhilling/graylog-plugin-function-multimatch)
[![Maven Central](https://img.shields.io/maven-central/v/de.hilling.graylog/graylog-plugin-function-multimatch.svg)](http://search.maven.org/#search|gav|1|g:"de.hilling.graylog"%20AND%20a:"graylog-plugin-function-multimatch")

# graylog multimatch function

## Graylog plugin for matching against multiple conditions

This plugin provides the function _multimatch_ that can be used to test a message dynamically against
conditions from a lookup function.

## Usage

The lookup must yield a java `List` containing `Map<String, String>` objects.

The `multimatch` evalutation yields true if any of the maps matches the contained conditions.

The maps may conatain one or more pairs. The evalutation is done as follows:

* If the message does not have a field matching the key, the result is `false`.
* If the key is _message_ the message field of the log message is compared via regexp matcher with the value.
* In all other cases it is compared via `Objects.equals()`.

## Example

A rule could be written as follows:

```
rule "apply blacklists"
when
  multimatch(matcherMap: lookup("blacklist", $message.source))
then
  set_field("backlisted", true);
  route_to_stream(name: "Blacklisted", remove_from_default: true);
end
```

With the lookup table yielding a result like:

```json
{
  "single_value": "source-system",
  "multi_value": {
    "value": [
      {
        "message": "^DEBUG: .*$"
      },
      {
        "message": "^INFO: .*$"
      }
    ]
  },
  "ttl": 9223372036854776000
}
```

`multi_value` will be used with the above rule. A message will match if starting with either `DEBUG`or `INFO`.

## Future development

Probably.
If you have any questions or proposals for enhancements please drop me a note.


## Credits

[LMIS AG](https://www.lmis.de) for the development of this plugin.

## LICENSE

 Copyright 2018 Gunnar Hilling

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

