# Welcome to my little raspery pi aquarium IoT project.

This project contains a java application which 
is designed to work on a rasperrypi and to read 
connected sensor data to provide them via a small
microservice in prometheus way.

Thus a prometheus instance can collect the data and you
will be able to display and analyse it via grafana.


## Rasperry Pi Setup

### Base-Setup

Burn a SD Card (16GB) with the latest Raspbian (CLI-Edition, no Desktop)
Connect the pi to a lan cable (only temorary), hdmi, keyboard

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

Do a 

    sudo shutdown -r now
    
and disconnect the Pi from the LAN. 
After 2min you should be able to ssh into the pi via WLAN.
If noit reconnect the LAN cable and check the WLAN settings
you provided via raspi-config.

### Build and deploy the microservice on the pi

(todo)

## Sensor-Endpoints

### DS18B20 Temperatur Sensor

curl http://localhost:8080/sensor/temp/ds18b20
Implement me :-)%

