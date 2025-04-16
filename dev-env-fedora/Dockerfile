# Start from Fedora 41
FROM fedora:41

ENV DISTRIBUTION_NAME=fedora

# Install Adoptium Repo 
RUN cat <<EOF > /etc/yum.repos.d/adoptium.repo
[Adoptium]
name=Adoptium
baseurl=https://packages.adoptium.net/artifactory/rpm/${DISTRIBUTION_NAME:-$(. /etc/os-release; echo $ID)}/\$releasever/\$basearch
enabled=1
gpgcheck=1
gpgkey=https://packages.adoptium.net/artifactory/api/gpg/key/public
EOF

# Install java and some common packages and update dependencies from image
RUN sudo dnf install --setopt=install_weak_deps=False temurin-23-jdk wget git gcc g++ -y && sudo dnf update -y

# Java Home configuration 
RUN cat <<EOF > /etc/bashrc
export JAVA_HOME=$((readlink -f /usr/bin/java) | xargs dirname | xargs dirname)
EOF

RUN source /etc/bashrc

# Download and install Maven 

RUN  wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz -P /tmp && tar -zxvf /tmp/apache-maven-3.9.9-bin.tar.gz -C /opt && ln -s /opt/apache-maven-3.9.9 /opt/maven

ENV M2_HOME="/opt/maven"
ENV MAVEN_HOME="/opt/maven"
ENV PATH="/opt/maven/bin:$PATH"

# Download and install glpk

RUN wget https://ftp.gnu.org/gnu/glpk/glpk-5.0.tar.gz -P /tmp && tar -zxvf /tmp/glpk-5.0.tar.gz -C /tmp && cd /tmp/glpk-5.0 && ./configure && make && make install

ENV LD_LIBRARY_PATH="/usr/local/lib"

# Download and install jextract
RUN wget https://download.java.net/java/early_access/jextract/22/6/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz -P /tmp && tar -zxvf /tmp/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz -C /opt && ln -s /opt/jextract-22 /opt/jextract

ENV PATH="/opt/jextract/bin:$PATH"

# Download and install SCIPOpt

RUN wget https://www.scipopt.org/download/release/scipoptsuite-9.2.1.tgz -P /tmp && tar -zxvf /tmp/scipoptsuite-9.2.1.tgz -C /opt && ln -s /opt/scipoptsuite-9.2.1 /opt/scipopt

RUN dnf install --setopt=install_weak_deps=False cmake coin-or-Ipopt-devel gmp-devel zlib-ng-devel zlib-ng-compat-devel readline-devel boost-devel tbb-devel -y
RUN mkdir /opt/scipopt/build && cd /opt/scipopt/build && cmake .. && make

# Download and install HIGHS 

RUN cd /opt/ && git clone --branch v1.10.0 --single-branch https://github.com/ERGO-Code/HiGHS.git && cd /opt/HiGHS && cmake -S . -B build && cmake --build build

# Cleanup downloads
RUN rm -rf /tmp/* 




