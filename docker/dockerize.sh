#! /usr/bin/env bash

usage() {
    echo "Usage: ${0} -u DOCKER_USER -p DOCKER_PASSWORD, where:" 1>&2
    echo 1>&2
    echo "  -u    User name for Docker Hub login" 1>&2
    echo "  -p    Password for Docker Hub login" 1>&2
    exit 1
}

while getopts ":u:p:" opt; do
    case "${opt}" in
        u)
            DOCKER_USER=${OPTARG}
            ;;
        p)
            DOCKER_PASSWORD=${OPTARG}
            ;;
        *)
            usage
            ;;
    esac
done
shift $((OPTIND-1))

if [ -z "${DOCKER_USER}" ]; then
    usage
fi

if [ -z "${DOCKER_PASSWORD}" ]; then
    read -s -p "Docker registry password for ${DOCKER_USER}: " DOCKER_PASSWORD
    echo
fi

docker login -u ${DOCKER_USER} --password-stdin <<< ${DOCKER_PASSWORD}
DIR=`dirname ${0}`
pushd ${DIR}/..

./gradlew shadowJar
SEMVER=`cat ${DIR}/../version.txt`
IMG=${DOCKER_USER}/waes-ta:${SEMVER}
docker build -t ${IMG} --build-arg semver=${SEMVER} -f docker/Dockerfile .
docker push ${IMG}

popd
