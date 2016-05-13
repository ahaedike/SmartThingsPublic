/**
 *  Front Porch Night Motion
 *
 *  Copyright 2016 Art Haedike
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Front Porch Night Motion",
    namespace: "ahaedike",
    author: "Art Haedike",
    description: "React to motion on front porch at night",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Turn on when there's movement..."){
		input "motion1", "capability.motionSensor", title: "Where?"
	}
	section("And off when there's been no movement for..."){
		input "minutes1", "number", title: "Minutes?"
	}
	section("Lights") {
        input "switches", "capability.switch", title: "Which lights to turn on?", multiple:true
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(motion1, "motion", motionHandler)
	subscribe(switches, "switch.on", switchOn)
    subscribe(location, "sunset", sunsetHandler)
}

def sunsetHandler(evt) {
    log.debug "turning on lights at sunset"
    switches.on()
    
    //Turn them off at midnight
    var d = new Date()+1
	d.setHours(0,0,0,0)
    runOnce(d, midnightHandler)
}

def midnightHandler(evt){
	switches.off()
}

def resetSwitches(evt){
 	def suntimes = getSunriseAndSunset()
    if (new Date() < suntimes.sunset) {  
    	switches.off()
    }	
}

def switchOn(evt) {
    log.debug "switchOn: $evt.name: $evt.value"
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
    def suntimes = getSunriseAndSunset()
    def now = new Date()
    if (evt.value == "active" && (now > suntimes.sunset || now < suntimes.sunrise)) {  
    	switches.on()
        def timeDelay = minutes1 * 60
        runIn (timeDelay, resetSwitches)
    }
}