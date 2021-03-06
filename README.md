# Welcome to my little raspery pi aquarium IoT project.

This project contains a java application which 
is designed to work on a rasperrypi and to read 
connected sensor data to provide them via a small
microservice in prometheus way.

Thus a prometheus instance can collect the data and you
will be able to display and analyse it via grafana.

**WARNING:** Having open electric circuits near saltwater can be potentially dangerous. Do not follow this setup unless you are experienced with this type
of work and know what you are doing and how to secure your setup. Meaning I take no responsibility or liability of your work.

## Rasperry Pi Setup

![](https://raw.githubusercontent.com/StefanSchubert/aquarium_IoT/main/assets/Components.png)


| Preview | Remark |
|---|---|
| ![](https://raw.githubusercontent.com/StefanSchubert/aquarium_IoT/main/assets/Board_Top.png) | I'm not sure how my sensor will react to the seawater. It might be that I need to replace it after a while so I choosed not to solder the wires onto the board and use the blue screw thing which makes it easy to mount a new one. |
| ![](https://raw.githubusercontent.com/StefanSchubert/aquarium_IoT/main/assets/Board_Bottom.png) | My first soldering work after 20 years. Please don't blame me. I choosed to leave a spare lane between to connected ones, as I didn't trusted my soldering capabilities.|
| ![](https://raw.githubusercontent.com/StefanSchubert/aquarium_IoT/main/assets/Finished.png) | This is the result. Meanwhile up and running :-) |
| ![](https://raw.githubusercontent.com/StefanSchubert/aquarium_IoT/main/assets/grafanaBoard.png) | And this is what it looks like with a simple grafana board displaying the values. By the way I'm using grafana to **set threshhold and email alerting**. | 


### Base-Setup

Burn a SD Card (16GB) with the latest Raspbian (CLI-Edition, no Desktop)
Connect the pi to a lan cable (only temporarily), hdmi, keyboard

* Connect to atlantis with default credentials (pi/raspberry)
* sudo raspi-config
    * Choose a hostname (for this sample atlantis)
    * Change login credentials
    * Activate SSH network service
    * Configure locale, keyboard, timezone 
    * Configure WLAN
    * In advance setup: activate the 1-wire protocol
    
Upgrade the system 

    sudo apt-get update
    sudo apt-get upgrade

Copy the ssh key of your workstation/laptop to the pi to ease ssh connect
(ommit this if you are not familiar with it)

    ssh-copy-id -i id_rsa.pub pi@atlantis

Configure unattended upgrades for security

    sudo apt-get install unattended-upgrades apt-listchanges
    sudo nano /etc/apt/apt.conf.d/50unattended-upgrades

The editor pops up, clear all an insert this:

    Unattended-Upgrade::Origins-Pattern {
            "origin=Raspbian,codename=${distro_codename},label=Raspbian";
            "origin=Raspberry Pi Foundation,codename=${distro_codename},label=Raspberry Pi Foundation";
    };
       
    Unattended-Upgrade::Mail "pi";
    Unattended-Upgrade::Automatic-Reboot "true"; 
    Unattended-Upgrade::Automatic-Reboot-Time "02:00";

Install some additional packages

    # So that unattended upgrades can deviler mails
    sudo apt-get install mailutils
      
    # and that your local user can read them on the console using mutt
    sudo apt-get install mutt
     
    # Java11 which is required to run our microservice
    sudo apt-get install openjdk-11-jre-headless

Create a own user for our microservice

    sudo adduser --home /var/aquametric --shell /usr/sbin/nologin aquametric

choose an arbitrary password and name like "aqua computer" when prompted and leave the rest as default.

Do a 

    sudo shutdown -r now
    
and disconnect the Pi from the LAN. 
After 2min you should be able to ssh into the pi via WLAN.
If noit reconnect the LAN cable and check the WLAN settings
you provided via raspi-config.

### Build and deploy the microservice on the pi

#### Build steps

Easy - to build the project you need java 11 and maven.
do a 

    mvn clean package 

and keep the target/aquametric-1.0-SNAPSHOT.jar

#### Deploy the version onto the pi via ansible

The deployment via ansible on single rasperry-pi's is like using a sledgehammer to crack a nut,
it is unsuitable here but I had it already a blueprint so it was easy to adopt.

##### Preconditions to use this deployment procedure

* Ansible and ssh are available
* The ssh private key of the executing user has been published onto the 'pi' account.

###### Examples

_Execute something on all pis_

	ansible aquapis -i hosts -u pi -a "/bin/uname -a"

or check the unattended update logs

	ansible aquapis -i hosts -u pi -a "tail -n20 /var/log/unattended-upgrades/unattended-upgrades.log"


##### Deployment

Build the new aquametrics release and then from the ansible dir

    ansible-playbook -i hosts deployAquaMetricsService.yml

wait 2 min and then do the endpoint test. Your service should be up and running and
do so even after restart.

## Sensor-Endpoints

### DS18B20 Temperatur Sensor

I built the pi DS18B20 circuit following e.g. http://tuxgraphics.org/npa/raspberry-pi-ds18s20-temperature-sensor/
The test readout must be working before continuing.

Above tutorial uses some perl programming, which seems quite effective.
But as I'm familiar with java I choosed to write a small java microservice
using spring-boot.  

#### Configuration

Tell your microservice which file to use to access your sensor

##### Edit application.properties

Notice the file exists twice

ansible/application.properties is the one wich will be used on the pi 
src/main/resources/application.properties will be taken on local deployment in you IDE.

    # Each sensor has it's own device ID. You will find it as sub-folder here: /sys/bus/w1/devices
    ds18b20.device.id=28-0319a2795781

##### Endpoint test 

    curl http://localhost:8080/sensor/temp/ds18b20
    
    # Temperature in celsius (will be exact 0.0 in case of errors)
    aqua_measure_celsius{sensor="28-0319a2795781"} 21.062
    # duration of measurement in millis
    aqua_measure_duration{sensor="28-0319a2795781"} 3

That's it :-) 

##### Prometheus / Grafana Config

Setting up of Prometheus/Grafana is not covered in this project. I assume you have
it already up and running, you may find my config helpful for integration.
As the temperature won't change so fast, a scrape interval of
5 mins should be sufficient.

###### Scrape-Config Prometheus

      # Atlantis aquametrics
      - job_name: 'aquametrics'
        metrics_path: '/sensor/temp/ds18b20'
        scrape_interval: 300s
        static_configs:
          - targets: ['atlantis:8080']
            labels:
              sensor: 'ds18b20'

###### Sample Grafana Board

Simple copy paste this as json export to got a simple dashboard 
for the sensor: assets/grafanaBoard.json

 



