#####
# Weblounge configuration
##

# Where your web applications are located
WEBLOUNGE_HOME="/usr/share/weblounge"

# Configuration for weblounge
WEBLOUNGE_CONF_DIR="/etc/weblounge"

# Work directory
WEBLOUNGE_DATA_DIR=/var/lib/weblounge

# Weblounge temporary files go here
WEBLOUNGE_TEMP_DIR="/var/tmp/weblounge"

# Where to put weblounge's log files
WEBLOUNGE_LOG_DIR="/var/log/weblounge"

# Weblounge sites directory
WEBLOUNGE_SITES_DIR="${WEBLOUNGE_DATA_DIR}/sites"

# Weblounge sites data directory
WEBLOUNGE_SITES_DATA_DIR="${WEBLOUNGE_DATA_DIR}/sites-data"

# Weblounge's user
WEBLOUNGE_USER="felix"

# Weblounge's group
WEBLOUNGE_GROUP="felix"


#####
# Felix configuration
##

# Where Felix will put the expanded bundles
FELIX_BUNDLECACHE_DIR="${WEBLOUNGE_TEMP_DIR}/felix-cache"

# Where to have fileinstall check for relevant items
FELIX_FILEINSTALL_DIR="-Dfelix.fileinstall.dir=${WEBLOUNGE_HOME}/load"

# Configuration admin's configuration location
PAX_CONFMAN_OPTS="-Dbundles.configuration.location=${WEBLOUNGE_CONF_DIR}"

# Configuration of the logging facility 
PAX_LOGGING_OPTS="-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN -Dweblounge.logdir=${WEBLOUNGE_LOG_DIR}"


#####
# Runtime environment setup
##

# The log file containing the init script's output
LOG_FILE="${WEBLOUNGE_LOG_DIR}/weblounge.out"

# The pid
PID_FILE="/var/run/weblounge.pid"


#####
# JVM configuration
##

# Set's JAVA_HOME to current selected JRE
JAVA_HOME="$(java-config --jre-home)"

# JVM Memory configuration parameter
JVM_MEMORY_OPTS="-Xmx1024m -Xms512m"

# Options to pass to the JVM on behalf of Jetty. Uncomment to disable debugging
#JVM_DEBUG_OPTS="-Xdebug -ea:ch -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

# Path to additional jre libraries
JVM_LIBRARY_PATH="-Djava.library.path=$(java-config -i sun-jai-bin)"

# Options for the advanced windowing toolkit
JVM_AWT_OPTS="-Djava.awt.headless=true -Dawt.toolkit=sun.awt.HeadlessToolkit"

# Set the vm encoding
JVM_ENCODING_OPTS="-Dfile.encoding=utf-8"


####
# Java options and commandline
##

# Where sites and site data go
WEBLOUNGE_DATA_OPTS="-Dweblounge.sitesdir=${WEBLOUNGE_SITES_DIR} -Dweblounge.sitesdatadir=${WEBLOUNGE_SITES_DATA_DIR}"

# The resulting weblounge commandline options
WEBLOUNGE_OPTS="${WEBLOUNGE_DATA_OPTS}"

# Aggregated commandline options for felix
FELIX_OPTS="${FELIX_FILEINSTALL_DIR} ${PAX_CONFMAN_OPTS} ${PAX_LOGGING_OPTS}"

# The resulting jvm-specific commandline options for java
JVM_OPTS="-server ${JVM_DEBUG_OPTS} ${JVM_MEMORY_OPTS} ${JVM_ENCODING_OPTS} ${JVM_LIBRARY_PATH} ${JVM_AWT_OPTS}"

# Final command
RUN_CMD="${JAVA_HOME}/bin/java"

# Final set of options to pass
RUN_OPTS="${JVM_OPTS} ${WEBLOUNGE_OPTS} ${FELIX_OPTS} -jar ${WEBLOUNGE_HOME}/bin/felix.jar ${FELIX_BUNDLECACHE_DIR}"