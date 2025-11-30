# Start from Fedora 42
FROM fedora:42

# Copy maven settings.xml
COPY settings.xml /root/.m2/settings.xml

# Install Adoptium Repo
COPY adoptium.repo /etc/yum.repos.d/adoptium.repo

# Install java and some common packages and update dependencies from image
RUN dnf install --setopt=install_weak_deps=False temurin-24-jdk wget git gcc g++ awk -y && \
    dnf update -y

# Java Home configuration 
RUN cat <<EOF > /etc/bashrc
export JAVA_HOME=$((readlink -f /usr/bin/java) | xargs dirname | xargs dirname)
EOF

RUN source /etc/bashrc

# Download and install Maven 

RUN wget https://dlcdn.apache.org/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.tar.gz -P /tmp && \
    tar -zxvf /tmp/apache-maven-3.9.11-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.11 /opt/maven

ENV M2_HOME="/opt/maven"
ENV MAVEN_HOME="/opt/maven"
ENV PATH="/opt/maven/bin:$PATH"

# Download and install glpk
# GLPK uses ANSI C standards and does not build with the latest versions of C because of bool typedef instantiations.
# Setup CFLAGS env variables during the configure and installation process to force using the ansi C standards for the
# GLPK builds.
ENV CFLAGS="-ansi"
RUN wget https://ftp.gnu.org/gnu/glpk/glpk-5.0.tar.gz -P /tmp && \
    tar -zxvf /tmp/glpk-5.0.tar.gz -C /tmp && \
    cd /tmp/glpk-5.0 && \
    ./configure && \
    make && \
    make install

ENV CFLAGS=""
ENV LD_LIBRARY_PATH="/usr/local/lib"

# Download and install jextract
RUN wget https://download.java.net/java/early_access/jextract/22/6/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz -P /tmp && \
    tar -zxvf /tmp/openjdk-22-jextract+6-47_linux-x64_bin.tar.gz -C /opt && \
    ln -s /opt/jextract-22 /opt/jextract

ENV PATH="/opt/jextract/bin:$PATH"

# Download and install SCIPOpt

RUN wget https://www.scipopt.org/download/release/scipoptsuite-10.0.0.tgz -P /tmp && \
    tar -zxvf /tmp/scipoptsuite-10.0.0.tgz -C /opt && \
    ln -s /opt/scipoptsuite-10.0.0 /opt/scipopt

RUN dnf install --setopt=install_weak_deps=False cmake coin-or-Ipopt-devel gmp-devel zlib-ng-devel zlib-ng-compat-devel readline-devel boost-devel tbb-devel mpfr-devel -y
RUN mkdir /opt/scipopt/build && \
    cd /opt/scipopt/build && \
    cmake .. && \
    make && \
    make install

# Download and install HIGHS

RUN cd /opt/ && \
    git clone --branch v1.12.0 --single-branch https://github.com/ERGO-Code/HiGHS.git && \
    cd /opt/HiGHS && \
    cmake -S . -B build && \
    cmake --build build

# Download and install Mosek

RUN wget https://download.mosek.com/stable/11.0.25/mosektoolslinux64x86.tar.bz2 -P /tmp && \
    tar -xvf /tmp/mosektoolslinux64x86.tar.bz2 -C /tmp && \
    mv /tmp/mosek/11.0 /opt/mosek

# Cleanup downloads
RUN rm -rf /tmp/*
