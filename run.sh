echo $1

sudo sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\(\s\+desh_postgres\s*\)$/'$1'\1/g' /etc/hosts
sudo sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\(\s\+kafka-[0-9]\s*\)$/'$1'\1/g' /etc/hosts
sudo sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\(\s\+nodeport-0\s*\)$/'$1'\1/g' /etc/hosts
sudo sed -i 's/[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+\(\s\+spark-master\s*\)$/'$1'\1/g' /etc/hosts

sed -i 's/\(ZOOKEEPER_URL=\)[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+/\1'$1'/g' exports.sh
sed -i 's/\(KAFKA_URL=\)[0-9]\+\.[0-9]\+\.[0-9]\+\.[0-9]\+/\1'$1'/g' exports.sh

source exports.sh
echo ZOOKEEPER_URL:
echo $ZOOKEEPER_URL
sudo mount -t nfs -o port=32049 $1:/ /mnt
sudo mkdir /mnt/casefiles
sudo chmod a+rw /mnt/casefiles
sleep 10
ant run
