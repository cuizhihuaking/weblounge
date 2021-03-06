#!/bin/bash
#
# This script is to be executed by Entwine Jenkins
# currently running at build.entwinemedia.com
# It takes 3 arguments that must be passed in the jenkins projects
# The arguments are, the jenkins workspace (see Jenkins doc)
# the customer name (used to create the customer repository)
# the customer rpm repository host FQDN
# the username for which ssh key payring has been configured for jenkins
# (this means /home/$username/.ssh/authorized_keys contains the jenkins public key
#
# With these 4 args this script will create the RPM package 
# that will be made available to the customer systems
# the archival of all builds but the latest 3 ones, 
# and the initialization of the mirroring of the repository at the customer site.

#usage jenkins-script.sh -w jenkins-workspace -c customer-name -m mirror-host-fqdn -u ssh-username

set -x 

# Script execution checks
echo "Time check: script starting"
date +%H\:%M

# Get Jenkins passed arguments
#
WORKSPACE=
CUSTOMER=
MIRROR_HOST=
SSH_USERNAME=
while getopts "c:w:m:u:" OPTION
do
     case $OPTION in
         w)
             WORKSPACE=$OPTARG
             ;;
         c)
             CUSTOMER=$OPTARG
             ;;
         m)
             MIRROR_HOST=$OPTARG
             ;;
         u)
             SSH_USERNAME=$OPTARG
             ;;
         ?)
             echo "Error in the script arguments exiting"
             exit 1
             ;;
esac
done

# Get the version and release
MVN_VERSION="$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)')"
RPM_VERSION="$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version|grep -Ev '(^\[|Download\w+:)' | awk '{split($0,a,"-"); print a[1]}')"
RPM_RELEASE="$(git log -1 --pretty=format:"%ad %h" --date=short|sed s/'[[:space:]]'/."$(git log --oneline|wc -l)"git/|sed s/-//g)"
cd "$WORKSPACE"

# Sanity check for production default config files replacement
if [ ! -d "docs/scripts/rpm" ];then
  echo "docs/scripts/rpm does not exist, check the branch used for the checkout"
  exit 1
fi

# Set version and release tag for the package
sed -i s#CHANGE_ME_VERSION#"$RPM_VERSION"# "docs/scripts/rpm/weblounge.spec"
sed -i s#CHANGE_ME_RELEASE#"$RPM_RELEASE"# "docs/scripts/rpm/weblounge.spec"
sed -i s#CHANGE_ME_VERSION#"$MVN_VERSION"# "docs/scripts/rpm/contents/etc/system.properties"
sed -i s#CHANGE_ME_VERSION#"$RPM_VERSION"# "docs/scripts/rpm/contents/etc/motd"

# Time check
echo "Starting the rpm build preparations"
date +%H\:%M

# Create a local user for this build
sudo useradd "$RPM_RELEASE"

# Remove what we don't want in the rpm
sudo rm -f "bin/start.sh"
sudo rm -f "bin/start.bat"

# Create the directory structure for putting together the RPMs
echo "Preparing the build environment at /home/$RPM_RELEASE/weblounge.$RPM_RELEASE"
sudo su - "$RPM_RELEASE" -c "rpmdev-setuptree"
sudo su - "$RPM_RELEASE" -c "mkdir -p /home/$RPM_RELEASE/weblounge.$RPM_RELEASE"
sudo su - "$RPM_RELEASE" -c "mkdir -p /home/$RPM_RELEASE/rpmbuild/{SOURCES,SPECS}"

# Move the files in place for the creation of the RPMs
echo "Moving the rpm contents to /home/$RPM_RELEASE/weblounge.$RPM_RELEASE"
sudo cp -r "bin" /home/"$RPM_RELEASE"/weblounge."$RPM_RELEASE"
sudo cp -r "docs" /home/"$RPM_RELEASE"/weblounge."$RPM_RELEASE"
sudo cp -r "docs/scripts/rpm/contents/etc" /home/"$RPM_RELEASE"/weblounge."$RPM_RELEASE"
sudo cp -r "lib" /home/"$RPM_RELEASE"/weblounge."$RPM_RELEASE"

# Switch to the rpm build directory
sudo su - "$RPM_RELEASE" -c "cd ~"

# Create the tarball for the rpm
echo "Creating a tarball at /home/$RPM_RELEASE/rpmbuild/SOURCES/weblounge.$RPM_RELEASE.tar.gz"
sudo su - "$RPM_RELEASE" -c "tar -cvzf rpmbuild/SOURCES/weblounge.$RPM_RELEASE.tar.gz weblounge.$RPM_RELEASE"
sudo chown "$RPM_RELEASE" /home/"$RPM_RELEASE"/rpmbuild/SOURCES/weblounge."$RPM_RELEASE".tar.gz

# Move the spec file to the release directory
sudo cp "docs/scripts/rpm/weblounge.spec" /home/"$RPM_RELEASE"/rpmbuild/SPECS
sudo chown $RPM_RELEASE /home/"$RPM_RELEASE"/rpmbuild/SPECS/weblounge.spec

# Create the rpm
echo "Starting the rpm build process"
date +%H\:%M

sudo su - "$RPM_RELEASE" -c "rpmbuild -ba rpmbuild/SPECS/weblounge.spec"
rpm_exit_code=$?
if [ ! $rpm_exit_code -eq 0 ];then
  echo "RPM Creation failed, check rpm log, exiting"
  sudo userdel -r "$RPM_RELEASE"
  exit 1
fi

# Move the rpm to the repository
echo "Moving the rpm to the rpm repository"
date +%H\:%M

sudo chmod 777 -R /home/"$RPM_RELEASE"/rpmbuild
sudo rm -rf /tmp/"$RPM_RELEASE"
mkdir /tmp/"$RPM_RELEASE"
chmod -R 777 /tmp/"$RPM_RELEASE"
sudo su "$RPM_RELEASE" -c "cp -r /home/$RPM_RELEASE/rpmbuild/SRPMS /tmp/$RPM_RELEASE/"
sudo su "$RPM_RELEASE" -c "cp -r /home/$RPM_RELEASE/rpmbuild/RPMS /tmp/$RPM_RELEASE/"
sudo mkdir -p /var/www/rpm-repos/$CUSTOMER/{SRPMS,RPMS}

# Move the packages to the repository
sudo cp -r /tmp/"$RPM_RELEASE"/SRPMS/* /var/www/rpm-repos/$CUSTOMER/SRPMS/
sudo cp -r /tmp/"$RPM_RELEASE"/RPMS/x86_64/* /var/www/rpm-repos/$CUSTOMER/RPMS

# Remove the user and its home directory (and with that all the rpmbuild work directory)
sudo userdel -r "$RPM_RELEASE"

# Delete debuginfo packages from customer repo
sudo rm -f /var/www/rpm-repos/$CUSTOMER/RPMS/*debuginfo*.rpm

# Delete all packages but the 3 newest ones (and don't care if there are less than 3)
cd /var/www/rpm-repos/$CUSTOMER/RPMS && sudo ls -t1 | tail -n +4 | sudo xargs rm -r 2> /dev/null
cd /var/www/rpm-repos/$CUSTOMER/SRPMS && sudo ls -t1 | tail -n +4 | sudo xargs rm -r 2> /dev/null

# Update the customer repo
sudo createrepo /var/www/rpm-repos/$CUSTOMER

#end of script
echo "Time check: script end"
date +%H:%M
exit 0
