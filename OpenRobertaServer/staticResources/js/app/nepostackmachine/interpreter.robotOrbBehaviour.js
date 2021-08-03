var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
define(["require", "exports", "./interpreter.aRobotBehaviour", "./interpreter.constants", "./interpreter.util"], function (require, exports, interpreter_aRobotBehaviour_1, C, U) {
    "use strict";
    Object.defineProperty(exports, "__esModule", { value: true });
    exports.RobotOrbBehaviour = void 0;
    var driveConfig = {
        "motorL": { "port": 1, "orientation": 1 },
        "motorR": { "port": 2, "orientation": 1 },
        "wheelDiameter": 5.6, "trackWidth": 22.8
    };
    var propFromORB = {
        "Motor": [{ "pwr": 0, "speed": 0, "pos": 0 },
            { "pwr": 0, "speed": 0, "pos": 0 },
            { "pwr": 0, "speed": 0, "pos": 0 },
            { "pwr": 0, "speed": 0, "pos": 0 }],
        "Sensor": [{ "valid": false, "type": 0, "option": 0, "value": [0, 0] },
            { "valid": false, "type": 0, "option": 0, "value": [0, 0] },
            { "valid": false, "type": 0, "option": 0, "value": [0, 0] },
            { "valid": false, "type": 0, "option": 0, "value": [0, 0] }],
        "Vcc": 0,
        "Digital": [false, false],
        "Status": 0
    };
    var cmdConfigToORB = {
        "target": "orb",
        "type": "data",
        "configToORB": {
            "Sensor": [{ "type": 0, "mode": 0, "option": 0 },
                { "type": 0, "mode": 0, "option": 0 },
                { "type": 0, "mode": 0, "option": 0 },
                { "type": 0, "mode": 0, "option": 0 }],
            "Motor": [{ "tics": 72, "acc": 50, "Kp": 50, "Ki": 30 },
                { "tics": 72, "acc": 50, "Kp": 50, "Ki": 30 },
                { "tics": 72, "acc": 50, "Kp": 50, "Ki": 30 },
                { "tics": 72, "acc": 50, "Kp": 50, "Ki": 30 }]
        }
    };
    var cmdPropToORB = {
        "target": "orb",
        "type": "data",
        "propToORB": {
            "Motor": [{ "mode": 0, "speed": 0, "pos": 0 },
                { "mode": 0, "speed": 0, "pos": 0 },
                { "mode": 0, "speed": 0, "pos": 0 },
                { "mode": 0, "speed": 0, "pos": 0 }],
            "Servo": [{ "mode": 0, "pos": 0 },
                { "mode": 0, "pos": 0 }]
        }
    };
    //Noch mode prüfen
    function isSensorValueValid(id) {
        if (propFromORB.Sensor[id].valid == true) {
            return true;
        }
        else {
            return false;
        }
    }
    function configSensor(id, type, mode, option) {
        id = id - 1;
        if (0 <= id && id < 4) {
            cmdConfigToORB.configToORB.Sensor[id].type = type;
            cmdConfigToORB.configToORB.Sensor[id].mode = mode;
            cmdConfigToORB.configToORB.Sensor[id].option = option;
            console.log("configSensor", "OK: " + "port=" + id + "," + JSON.stringify(cmdConfigToORB.configToORB.Sensor[id]));
        }
        else
            console.log("configSensor", "Err:wrong id");
    }
    function getSensorValue(id) {
        id = id - 1;
        if (0 <= id && id < 4) {
            if (isSensorValueValid(id) == true) {
                return (propFromORB.Sensor[id].value[0]);
            }
        }
        return 0;
    }
    function getSensorValueColor(id) {
        id = id - 1;
        if (0 <= id && id < 4) {
            if (isSensorValueValid(id) == true) {
                if (propFromORB.Sensor[id].value[0] == 0) {
                    return ("No Color");
                }
                if (propFromORB.Sensor[id].value[0] == 1) {
                    return ("Black");
                }
                if (propFromORB.Sensor[id].value[0] == 2) {
                    return ("Blue");
                }
                if (propFromORB.Sensor[id].value[0] == 3) {
                    return ("Green");
                }
                if (propFromORB.Sensor[id].value[0] == 4) {
                    return ("Yellow");
                }
                if (propFromORB.Sensor[id].value[0] == 5) {
                    return ("RED");
                }
                if (propFromORB.Sensor[id].value[0] == 6) {
                    return ("White");
                }
                if (propFromORB.Sensor[id].value[0] == 7) {
                    return ("Brown");
                }
            }
        }
        return (0);
    }
    function getSensorValueUltrasonic(id) {
        id = id - 1;
        if (0 <= id && id < 4) {
            if (isSensorValueValid(id) == true) {
                var a = propFromORB.Sensor[id].value[0];
                return (a / 10);
            }
        }
        return (0);
    }
    function getSensorValueGyro(id) {
        id = id - 1;
        if (0 <= id && id < 4) {
            if (isSensorValueValid(id) == true) {
                if (propFromORB.Sensor[id].value[0] <= 32767) {
                    return (propFromORB.Sensor[id].value);
                }
                else {
                    propFromORB.Sensor[id].value[0] = propFromORB.Sensor[id].value[0] - 65536;
                    return (propFromORB.Sensor[id].value);
                }
            }
        }
        return (0);
    }
    function getSensorValueTouch(id) {
        id = id - 1;
        if (0 <= id && id < 4) {
            if (isSensorValueValid(id) == true) {
                if (propFromORB.Sensor[id].value[0] == 1) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }
        return (0);
    }
    function getEncoderValue(port) {
        return (getMotorPos(port));
    }
    function setMotor(id, mode, speed, pos) {
        id = id - 1;
        if (0 <= id && id < 4) {
            cmdPropToORB.propToORB.Motor[id].mode = mode;
            cmdPropToORB.propToORB.Motor[id].speed = Math.floor(speed);
            cmdPropToORB.propToORB.Motor[id].pos = Math.floor(pos);
            console.log("setMotor", "OK: " + "port=" + id + "," + JSON.stringify(cmdPropToORB.propToORB.Motor[id]));
        }
        else
            console.log("setMotor", "Err:wrong id");
    }
    function getMotorPos(id) {
        id = id - 1;
        if (0 <= id && id < 4) {
            return (propFromORB.Motor[id].pos);
        }
        return (0);
    }
    var RobotOrbBehaviour = /** @class */ (function (_super) {
        __extends(RobotOrbBehaviour, _super);
        function RobotOrbBehaviour(btInterfaceFct, toDisplayFct) {
            var _this = _super.call(this) || this;
            _this.orb = {};
            _this.tiltMode = {
                UP: '3.0',
                DOWN: '9.0',
                BACK: '5.0',
                FRONT: '7.0',
                NO: '0.0'
            };
            _this.getConnectedBricks = function () {
                var brickids = [];
                for (var brickid in this.orb) {
                    if (this.orb.hasOwnProperty(brickid)) {
                        brickids.push(brickid);
                    }
                }
                return brickids;
            };
            _this.getBrickIdByName = function (name) {
                for (var brickid in this.orb) {
                    if (this.orb.hasOwnProperty(brickid)) {
                        if (this.orb[brickid].brickname === name.toUpperCase()) {
                            return brickid;
                        }
                    }
                }
                return null;
            };
            _this.getBrickById = function (id) {
                return this.orb[id];
            };
            _this.clearDisplay = function () {
                U.debug('clear display');
                this.toDisplayFct({ "clear": true });
            };
            _this.getSample = function (s, name, sensor, port, slot) {
                if (sensor == "ultrasonic") {
                    cmdConfigToORB.configToORB.Sensor[port - 1].type = 1;
                    if (slot == "distance") {
                        configSensor(port, 1, 0, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValueUltrasonic(port));
                    }
                    else if (slot == "presence") {
                        configSensor(port, 1, 2, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValue(port));
                    }
                }
                else if (sensor == "color") {
                    if (slot == "colour") {
                        configSensor(port, 1, 2, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValueColor(port));
                    }
                    if (slot == "light") {
                        configSensor(port, 1, 0, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValue(port));
                    }
                    if (slot == "ambientlight") {
                        configSensor(port, 1, 1, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValue(port));
                    }
                    if (slot == "rgb") {
                        configSensor(port, 1, 4, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValueColor(port));
                    }
                }
                else if (sensor == "touch") {
                    configSensor(port, 4, 0, 0);
                    this.btInterfaceFct(cmdConfigToORB);
                    s.push(getSensorValueTouch(port));
                }
                else if (sensor == "gyro") {
                    if (slot == "angle") {
                        configSensor(port, 1, 0, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValueGyro(port));
                    }
                    if (slot == "rate") {
                        configSensor(port, 1, 1, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValueGyro(port));
                    }
                }
                else if (sensor == "infrared") {
                    if (slot == "distance") {
                        configSensor(port, 1, 0, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValue(port));
                    }
                    if (slot == "presence") {
                        configSensor(port, 1, 2, 0);
                        this.btInterfaceFct(cmdConfigToORB);
                        s.push(getSensorValue(port));
                    }
                }
                else if (sensor == C.TIMER) {
                    s.push(this.timerGet(port));
                    return;
                }
                else if (sensor == "encoder") {
                    s.push(getEncoderValue(this.mappPortMotor(port)));
                }
                return;
            };
            _this.finalName = function (notNormalized) {
                if (notNormalized !== undefined) {
                    return notNormalized.replace(/\s/g, '').toLowerCase();
                }
                else {
                    U.info("sensor name undefined");
                    return "undefined";
                }
            };
            _this.btInterfaceFct = btInterfaceFct;
            _this.toDisplayFct = toDisplayFct;
            _this.timers = {};
            _this.timers['start'] = Date.now();
            U.loggingEnabled(true, true);
            return _this;
        }
        RobotOrbBehaviour.prototype.update = function (data) {
            U.info('update type:' + data.type + ' state:' + data.state + ' sensor:' + data.sensor + ' actor:' + data.actuator);
            if (data.target !== "orb") {
                return;
            }
            switch (data.type) {
                case "connect":
                    if (data.state == "connected") {
                        this.orb[data.brickid] = {};
                        this.orb[data.brickid]["brickname"] = data.brickname.replace(/\s/g, '').toUpperCase();
                        // for some reason we do not get the inital state of the button, so here it is hardcoded
                        this.orb[data.brickid]["button"] = 'false';
                    }
                    else if (data.state == "disconnected") {
                        delete this.orb[data.brickid];
                    }
                    break;
                case "didAddService":
                    var theOrbA = this.orb[data.brickid];
                    if (data.state == "connected") {
                        if (data.id && data.sensor) {
                            theOrbA[data.id] = {};
                            theOrbA[data.id][this.finalName(data.sensor)] = '';
                        }
                        else if (data.id && data.actuator) {
                            theOrbA[data.id] = {};
                            theOrbA[data.id][this.finalName(data.actuator)] = '';
                        }
                        else if (data.sensor) {
                            theOrbA[this.finalName(data.sensor)] = '';
                        }
                        else {
                            theOrbA[this.finalName(data.actuator)] = '';
                        }
                    }
                    break;
                case "didRemoveService":
                    if (data.id) {
                        delete this.orb[data.brickid][data.id];
                    }
                    else if (data.sensor) {
                        delete this.orb[data.brickid][this.finalName(data.sensor)];
                    }
                    else {
                        delete this.orb[data.brickid][this.finalName(data.actuator)];
                    }
                    break;
                case "update":
                    var theOrbU = this.orb[data.brickid];
                    if (data.id) {
                        if (theOrbU[data.id] === undefined) {
                            theOrbU[data.id] = {};
                        }
                        theOrbU[data.id][this.finalName(data.sensor)] = data.state;
                    }
                    else {
                        theOrbU[this.finalName(data.sensor)] = data.state;
                    }
                    break;
                case "data":
                    propFromORB = data.propFromORB;
                    break;
                default:
                    // TODO think about what could happen here.
                    break;
            }
            U.info(this.orb);
        };
        ;
        RobotOrbBehaviour.prototype.mappPortMotor = function (port) {
            //From MotorBlock come only letter, It must be mapped to numbers.
            //Think of a better solution
            if (port == 'a') {
                return 1;
            }
            if (port == 'b') {
                return 2;
            }
            if (port == 'c') {
                return 3;
            }
            if (port == 'd') {
                return 4;
            }
        };
        RobotOrbBehaviour.prototype.motorOnAction = function (name, port, duration, speed) {
            port = this.mappPortMotor(port);
            U.debug('motorOnAction' + ' port:' + port + ' duration:' + duration + ' speed:' + speed);
            var gradToTics = 1000.0 / 360.0;
            var timeToGo = 0;
            if (duration === undefined) {
                return (this.setMoveToMotorOnProcent(port, speed, 1));
            }
            else {
                return (this.setMoveToMotorOnProcent(port, speed, duration));
            }
            return 0;
        };
        RobotOrbBehaviour.prototype.motorStopAction = function (name, port) {
            U.debug('motorStopAction' + ' port:' + port);
            setMotor(port, 0, 0, 0);
            this.btInterfaceFct(cmdPropToORB);
            return 0;
        };
        RobotOrbBehaviour.prototype.driveAction = function (name, direction, speed, distance) {
            U.debug('driveAction' + ' direction:' + direction + ' speed:' + speed + ' distance:' + distance);
            if ((direction == C.BACKWARD) || (direction == "BACKWARD")) {
                speed *= -1;
            }
            if (distance === undefined) {
                return (this.setSpeedProcent(speed, speed));
            }
            else {
                if (speed < 0) {
                    distance *= -1;
                }
                return (this.setMoveToProcent(speed, speed, distance, distance));
            }
            return 0;
        };
        RobotOrbBehaviour.prototype.curveAction = function (name, direction, speedL, speedR, distance) {
            U.debug('curveAction' + ' direction:' + direction + ' speedL:' + speedL + ' speedR:' + speedR + ' distance:' + distance);
            if ((direction == C.BACKWARD) || (direction == "BACKWARD")) {
                speedL *= -1;
                speedR *= -1;
            }
            if (distance === undefined) {
                this.setSpeedProcent(speedL, speedR);
                return 0;
            }
            else {
                var speed = 0.5 * (Math.abs(speedL) + Math.abs(speedR));
                if (speed > 0) {
                    var distL = speedL * distance / speed;
                    var distR = speedR * distance / speed;
                    return (this.setMoveToProcent(speedL, speedR, distL, distR));
                }
            }
            return 0;
        };
        RobotOrbBehaviour.prototype.turnAction = function (name, direction, speed, angle) {
            U.debug('turnAction' + ' direction:' + direction + ' speed:' + speed + ' angle:' + angle);
            if (direction == C.LEFT) {
                speed *= -1;
            }
            if (angle === undefined) {
                this.setSpeedProcent(speed, -speed);
                return 0;
            }
            else {
                if (speed < 0) {
                    angle *= -1;
                }
                var distance = angle * Math.PI / 360 * driveConfig.trackWidth;
                return (this.setMoveToProcent(speed, speed, distance, -distance));
            }
            return 0;
        };
        RobotOrbBehaviour.prototype.setSpeedProcent = function (speedL, speedR) {
            var distanceToTics = 1000.0 / (driveConfig.wheelDiameter * Math.PI);
            var maxspeed = 2.7 * (driveConfig.wheelDiameter * Math.PI);
            speedL = ((speedL * maxspeed) / 100) * distanceToTics;
            speedR = ((speedR * maxspeed) / 100) * distanceToTics;
            setMotor(driveConfig.motorL.port, 2, driveConfig.motorL.orientation * speedL, 0);
            setMotor(driveConfig.motorR.port, 2, driveConfig.motorR.orientation * speedR, 0);
            this.btInterfaceFct(cmdPropToORB);
            return 0;
        };
        RobotOrbBehaviour.prototype.calcTimeToGo = function (speed, distance) {
            var t = 20000 / 50 + 200; // 50 = acc, 200 Reserve
            if (speed != 0) {
                t += 1000.0 * Math.abs(distance / speed);
            }
            return (t);
        };
        RobotOrbBehaviour.prototype.setMoveToProcent = function (speedL, speedR, deltaL, deltaR) {
            var distanceToTics = 1000.0 / (driveConfig.wheelDiameter * Math.PI);
            var maxspeed = 2.7 * (driveConfig.wheelDiameter * Math.PI);
            deltaL *= distanceToTics;
            deltaR *= distanceToTics;
            speedL = Math.abs(((speedL * maxspeed) / 100) * distanceToTics);
            speedR = Math.abs(((speedR * maxspeed) / 100) * distanceToTics);
            var targetL = getMotorPos(driveConfig.motorL.port) + driveConfig.motorL.orientation * deltaL;
            var targetR = getMotorPos(driveConfig.motorR.port) + driveConfig.motorR.orientation * deltaR;
            var timeToGoL = this.calcTimeToGo(speedL, deltaL);
            var timeToGoR = this.calcTimeToGo(speedR, deltaR);
            setMotor(driveConfig.motorL.port, 3, speedL, targetL);
            setMotor(driveConfig.motorR.port, 3, speedR, targetR);
            this.btInterfaceFct(cmdPropToORB);
            return (Math.max(timeToGoL, timeToGoR));
        };
        RobotOrbBehaviour.prototype.setSpeedMotorOnProcent = function (port, speed, duration) {
            var distanceToTics = 1000.0 / (driveConfig.wheelDiameter * Math.PI);
            var maxspeed = 2.7 * (driveConfig.wheelDiameter * Math.PI);
            speed = ((speed * maxspeed) / 100) * distanceToTics;
            setMotor(port, 2, driveConfig.motorL.orientation * speed, duration);
            this.btInterfaceFct(cmdPropToORB);
        };
        RobotOrbBehaviour.prototype.setMoveToMotorOnProcent = function (port, speed, delta) {
            delta = delta * (2 * driveConfig.wheelDiameter * Math.PI);
            var maxspeed = 2.7 * (driveConfig.wheelDiameter * Math.PI);
            var distanceToTics = 1000.0 / (driveConfig.wheelDiameter * Math.PI);
            delta *= distanceToTics;
            speed = Math.abs(((speed / 100) * maxspeed) * distanceToTics);
            var target = getMotorPos(port) + driveConfig.motorL.orientation * delta;
            var timeToGo = this.calcTimeToGo(speed, delta);
            setMotor(port, 3, speed, target);
            this.btInterfaceFct(cmdPropToORB);
            return (Math.max(timeToGo));
        };
        RobotOrbBehaviour.prototype.driveStop = function (_name) {
            this.btInterfaceFct(cmdConfigToORB);
            setMotor(driveConfig.motorL.port, 0, 0, 0);
            setMotor(driveConfig.motorR.port, 0, 0, 0);
            this.btInterfaceFct(cmdPropToORB);
        };
        RobotOrbBehaviour.prototype.timerReset = function (port) {
            this.timers[port] = Date.now();
            U.debug('timerReset for ' + port);
        };
        RobotOrbBehaviour.prototype.timerGet = function (port) {
            var now = Date.now();
            var startTime = this.timers[port];
            if (startTime === undefined) {
                startTime = this.timers['start'];
            }
            var delta = now - startTime;
            U.debug('timerGet for ' + port + ' returned ' + delta);
            return delta;
        };
        RobotOrbBehaviour.prototype.ledOnAction = function (name, port, color) {
            var brickid = this.getBrickIdByName(name);
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led on color ' + color);
            var cmd = { 'target': 'orb', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': color };
            this.btInterfaceFct(cmd);
        };
        RobotOrbBehaviour.prototype.statusLightOffAction = function (name, port) {
            var brickid = this.getBrickIdByName(name);
            var robotText = 'robot: ' + name + ', port: ' + port;
            U.debug(robotText + ' led off');
            var cmd = { 'target': 'orb', 'type': 'command', 'actuator': 'light', 'brickid': brickid, 'color': 0 };
            this.btInterfaceFct(cmd);
        };
        RobotOrbBehaviour.prototype.toneAction = function (name, frequency, duration) {
            throw new Error("Method not implemented.");
            return 0;
        };
        RobotOrbBehaviour.prototype.showImageAction = function (_text, _mode) {
            throw new Error("Method not implemented.");
            return 0;
        };
        RobotOrbBehaviour.prototype.displaySetBrightnessAction = function (_value) {
            return 0;
        };
        RobotOrbBehaviour.prototype.showTextActionPosition = function (text, x, y) {
            var showText = "" + text;
            U.debug('***** show "' + showText + '" *****');
            this.toDisplayFct({ "show": showText });
        };
        RobotOrbBehaviour.prototype.showTextAction = function (text, _mode) {
            var showText = "" + text;
            U.debug('***** show "' + showText + '" *****');
            this.toDisplayFct({ "show": showText });
            return 0;
        };
        RobotOrbBehaviour.prototype.setConfiguration = function (configuration) {
            driveConfig.trackWidth = configuration.TRACKWIDTH;
            driveConfig.wheelDiameter = configuration.WHEELDIAMETER;
            this.setConfigMotors(configuration.ACTUATORS);
            this.setConfigSensors(configuration.SENSORS);
            this.wait(3);
            return 0;
        };
        RobotOrbBehaviour.prototype.setConfigMotors = function (motors) {
            if ((motors.A != undefined) && (motors.A.MOTOR_DRIVE == "RIGHT")) {
                if (motors.A.MOTOR_REVERSE == "ON") {
                    driveConfig.motorR.orientation = -1;
                }
                driveConfig.motorR.port = 1;
            }
            if ((motors.A != undefined) && (motors.A.MOTOR_DRIVE == "LEFT")) {
                if (motors.A.MOTOR_REVERSE == "ON") {
                    driveConfig.motorL.orientation = -1;
                }
                driveConfig.motorL.port = 1;
            }
            if ((motors.B != undefined) && (motors.B.MOTOR_DRIVE == "RIGHT")) {
                if (motors.B.MOTOR_REVERSE == "ON") {
                    driveConfig.motorR.orientation = -1;
                }
                driveConfig.motorR.port = 2;
            }
            if ((motors.B != undefined) && (motors.B.MOTOR_DRIVE == "LEFT")) {
                if (motors.B.MOTOR_REVERSE == "ON") {
                    driveConfig.motorL.orientation = -1;
                }
                driveConfig.motorL.port = 2;
            }
            if ((motors.C != undefined) && (motors.C.MOTOR_DRIVE == "RIGHT")) {
                if (motors.C.MOTOR_REVERSE == "ON") {
                    driveConfig.motorR.orientation = -1;
                }
                driveConfig.motorR.port = 3;
            }
            if ((motors.C != undefined) && (motors.C.MOTOR_DRIVE == "LEFT")) {
                if (motors.C.MOTOR_REVERSE == "ON") {
                    driveConfig.motorL.orientation = -1;
                }
                driveConfig.motorL.port = 3;
            }
            if ((motors.D != undefined) && (motors.D.MOTOR_DRIVE == "RIGHT")) {
                if (motors.D.MOTOR_REVERSE == "ON") {
                    driveConfig.motorR.orientation = -1;
                }
                driveConfig.motorR.port = 4;
            }
            if ((motors.D != undefined) && (motors.D.MOTOR_DRIVE == "LEFT")) {
                if (motors.D.MOTOR_REVERSE == "ON") {
                    driveConfig.motorL.orientation = -1;
                }
                driveConfig.motorL.port = 4;
            }
            return 0;
        };
        RobotOrbBehaviour.prototype.setConfigSensors = function (sensors) {
            for (var i = 1; i < 5; i++) {
                if (sensors[i] == "TOUCH") {
                    configSensor(i, 4, 0, 0);
                    this.btInterfaceFct(cmdConfigToORB);
                }
                if ((sensors[i] == "ULTRASONIC") || (sensors[i] == "GYRO") || (sensors[i] == "INFRARED")) {
                    configSensor(i, 1, 0, 0);
                    this.btInterfaceFct(cmdConfigToORB);
                }
                if (sensors[i] == "COLOR") {
                    configSensor(i, 1, 2, 0);
                    this.btInterfaceFct(cmdConfigToORB);
                }
            }
            return 0;
        };
        RobotOrbBehaviour.prototype.wait = function (seconds) {
            var stopTime = (new Date()).getSeconds();
            stopTime = ((stopTime + seconds) < 60) ? stopTime + seconds : seconds - (60 - stopTime);
            while ((new Date()).getSeconds() < stopTime)
                ;
        };
        RobotOrbBehaviour.prototype.writePinAction = function (_pin, _mode, _value) {
        };
        RobotOrbBehaviour.prototype.close = function () {
            var ids = this.getConnectedBricks();
            for (var id in ids) {
                if (ids.hasOwnProperty(id)) {
                    var name_1 = this.getBrickById(ids[id]).brickname;
                    this.motorStopAction(name_1, 1);
                    this.motorStopAction(name_1, 2);
                    this.ledOnAction(name_1, 99, 3);
                }
            }
        };
        RobotOrbBehaviour.prototype.encoderReset = function (port) {
            U.debug('encoderReset for ' + port);
            this.hardwareState.actions.encoder = {};
            if (port == C.MOTOR_LEFT) {
                this.hardwareState.actions.encoder.leftReset = true;
            }
            else {
                this.hardwareState.actions.encoder.rightReset = true;
            }
        };
        RobotOrbBehaviour.prototype.gyroReset = function (_port) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.lightAction = function (_mode, _color, _port) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.playFileAction = function (_file) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype._setVolumeAction = function (_volume) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype._getVolumeAction = function (_s) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.setLanguage = function (_language) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.sayTextAction = function (_text, _speed, _pitch) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.getMotorSpeed = function (_s, _name, _port) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.setMotorSpeed = function (_name, _port, _speed) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.displaySetPixelBrightnessAction = function (_x, _y, _brightness) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.displayGetPixelBrightnessAction = function (_s, _x, _y) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.displayGetBrightnessAction = function (_volume) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.setVolumeAction = function (_volume) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.getVolumeAction = function (_s) {
            throw new Error("Method not implemented.");
        };
        RobotOrbBehaviour.prototype.debugAction = function (_value) {
            this.showTextAction("> " + _value, undefined);
        };
        RobotOrbBehaviour.prototype.assertAction = function (_msg, _left, _op, _right, _value) {
            if (!_value) {
                this.showTextAction("> Assertion failed: " + _msg + " " + _left + " " + _op + " " + _right, undefined);
            }
        };
        return RobotOrbBehaviour;
    }(interpreter_aRobotBehaviour_1.ARobotBehaviour));
    exports.RobotOrbBehaviour = RobotOrbBehaviour;
});
