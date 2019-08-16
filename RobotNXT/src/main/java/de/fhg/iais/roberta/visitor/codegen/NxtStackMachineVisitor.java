package de.fhg.iais.roberta.visitor.codegen;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import de.fhg.iais.roberta.components.Configuration;
import de.fhg.iais.roberta.components.ConfigurationComponent;
import de.fhg.iais.roberta.inter.mode.action.IDriveDirection;
import de.fhg.iais.roberta.inter.mode.action.ILanguage;
import de.fhg.iais.roberta.inter.mode.action.ITurnDirection;
import de.fhg.iais.roberta.mode.action.DriveDirection;
import de.fhg.iais.roberta.mode.action.Language;
import de.fhg.iais.roberta.mode.action.TurnDirection;
import de.fhg.iais.roberta.syntax.MotorDuration;
import de.fhg.iais.roberta.syntax.Phrase;
import de.fhg.iais.roberta.syntax.SC;
import de.fhg.iais.roberta.syntax.action.display.ClearDisplayAction;
import de.fhg.iais.roberta.syntax.action.display.ShowTextAction;
import de.fhg.iais.roberta.syntax.action.light.LightAction;
import de.fhg.iais.roberta.syntax.action.light.LightStatusAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorGetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorOnAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorSetPowerAction;
import de.fhg.iais.roberta.syntax.action.motor.MotorStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.CurveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.DriveAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.MotorDriveStopAction;
import de.fhg.iais.roberta.syntax.action.motor.differential.TurnAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayFileAction;
import de.fhg.iais.roberta.syntax.action.sound.PlayNoteAction;
import de.fhg.iais.roberta.syntax.action.sound.ToneAction;
import de.fhg.iais.roberta.syntax.action.sound.VolumeAction;
import de.fhg.iais.roberta.syntax.lang.expr.ColorConst;
import de.fhg.iais.roberta.syntax.lang.stmt.AssertStmt;
import de.fhg.iais.roberta.syntax.lang.stmt.DebugAction;
import de.fhg.iais.roberta.syntax.sensor.generic.*;
import de.fhg.iais.roberta.util.dbc.Assert;
import de.fhg.iais.roberta.visitor.C;
import de.fhg.iais.roberta.visitor.hardware.INxtVisitor;
import de.fhg.iais.roberta.visitor.lang.codegen.AbstractStackMachineVisitor;

public class NxtStackMachineVisitor<V> extends AbstractStackMachineVisitor<V> implements INxtVisitor<V> {

    private NxtStackMachineVisitor(Configuration configuration, ArrayList<ArrayList<Phrase<Void>>> phrases, ILanguage language) {
        super(configuration);
        Assert.isTrue(!phrases.isEmpty());

    }

    public static String generate(Configuration brickConfiguration, ArrayList<ArrayList<Phrase<Void>>> phrasesSet, ILanguage language) {
        Assert.isTrue(!phrasesSet.isEmpty());
        Assert.notNull(brickConfiguration);

        NxtStackMachineVisitor<Void> astVisitor = new NxtStackMachineVisitor<>(brickConfiguration, phrasesSet, language);
        astVisitor.generateCodeFromPhrases(phrasesSet);
        JSONObject generatedCode = new JSONObject();
        generatedCode.put(C.OPS, astVisitor.opArray).put(C.FUNCTION_DECLARATION, astVisitor.fctDecls);
        return generatedCode.toString(2);
    }

    @Override
    public V visitColorConst(ColorConst<V> colorConst) {
        int r = colorConst.getRedChannelInt();
        int g = colorConst.getGreenChannelInt();
        int b = colorConst.getBlueChannelInt();

        JSONObject o = mk(C.EXPR).put(C.EXPR, "COLOR_CONST").put(C.VALUE, new JSONArray(Arrays.asList(r, g, b)));
        return app(o);
    }

    @Override
    public V visitClearDisplayAction(ClearDisplayAction<V> clearDisplayAction) {
        JSONObject o = mk(C.CLEAR_DISPLAY_ACTION);

        return app(o);
    }

    @Override
    public V visitLightStatusAction(LightStatusAction<V> lightStatusAction) {
        JSONObject o = mk(C.STATUS_LIGHT_ACTION).put(C.NAME, "ev3").put(C.PORT, "internal");
        return app(o);
    }

    @Override
    public V visitToneAction(ToneAction<V> toneAction) {
        toneAction.getFrequency().visit(this);
        toneAction.getDuration().visit(this);
        JSONObject o = mk(C.TONE_ACTION);
        return app(o);
    }

    @Override
    public V visitPlayNoteAction(PlayNoteAction<V> playNoteAction) {
        String freq = playNoteAction.getFrequency();
        String duration = playNoteAction.getDuration();
        app(mk(C.EXPR).put(C.EXPR, C.NUM_CONST).put(C.VALUE, freq));
        app(mk(C.EXPR).put(C.EXPR, C.NUM_CONST).put(C.VALUE, duration));
        JSONObject o = mk(C.TONE_ACTION);
        return app(o);
    }

    @Override
    public V visitPlayFileAction(PlayFileAction<V> playFileAction) {
        String image = playFileAction.getFileName().toString();
        JSONObject o = mk(C.PLAY_FILE_ACTION).put(C.FILE, image).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitVolumeAction(VolumeAction<V> volumeAction) {
        JSONObject o;
        if ( volumeAction.getMode() == VolumeAction.Mode.GET ) {
            o = mk(C.GET_VOLUME);
        } else {
            volumeAction.getVolume().visit(this);
            o = mk(C.SET_VOLUME_ACTION);
        }
        return app(o);
    }

    @Override
    public V visitMotorGetPowerAction(MotorGetPowerAction<V> motorGetPowerAction) {
        String port = motorGetPowerAction.getUserDefinedPort();
        JSONObject o = mk(C.MOTOR_GET_POWER).put(C.PORT, port.toLowerCase());
        return app(o);
    }

    @Override
    public V visitDriveAction(DriveAction<V> driveAction) {
        driveAction.getParam().getSpeed().visit(this);
        MotorDuration<V> duration = driveAction.getParam().getDuration();
        appendDuration(duration);
        ConfigurationComponent leftMotor = this.configuration.getFirstMotor(SC.LEFT);
        IDriveDirection leftMotorRotationDirection = DriveDirection.get(leftMotor.getProperty(SC.MOTOR_REVERSE));
        DriveDirection driveDirection = (DriveDirection) driveAction.getDirection();
        if ( leftMotorRotationDirection != DriveDirection.FOREWARD ) {
            driveDirection = getDriveDirection(driveAction.getDirection() == DriveDirection.FOREWARD);
        }
        JSONObject o = mk(C.DRIVE_ACTION).put(C.DRIVE_DIRECTION, driveDirection).put(C.NAME, "ev3");
        if ( duration != null ) {
            app(o);
            return app(mk(C.STOP_DRIVE).put(C.NAME, "ev3"));
        } else {
            return app(o);
        }
    }

    @Override
    public V visitTurnAction(TurnAction<V> turnAction) {
        turnAction.getParam().getSpeed().visit(this);
        MotorDuration<V> duration = turnAction.getParam().getDuration();
        appendDuration(duration);
        ConfigurationComponent leftMotor = this.configuration.getFirstMotor(SC.LEFT);
        IDriveDirection leftMotorRotationDirection = DriveDirection.get(leftMotor.getProperty(SC.MOTOR_REVERSE));
        ITurnDirection turnDirection = turnAction.getDirection();
        if ( leftMotorRotationDirection != DriveDirection.FOREWARD ) {
            turnDirection = getTurnDirection(turnAction.getDirection() == TurnDirection.LEFT);
        }
        JSONObject o = mk(C.TURN_ACTION).put(C.TURN_DIRECTION, turnDirection.toString().toLowerCase()).put(C.NAME, "ev3");
        if ( duration != null ) {
            app(o);
            return app(mk(C.STOP_DRIVE).put(C.NAME, "ev3"));
        } else {
            return app(o);
        }

    }

    @Override
    public V visitCurveAction(CurveAction<V> curveAction) {
        curveAction.getParamLeft().getSpeed().visit(this);
        curveAction.getParamRight().getSpeed().visit(this);
        MotorDuration<V> duration = curveAction.getParamLeft().getDuration();
        appendDuration(duration);
        ConfigurationComponent leftMotor = this.configuration.getFirstMotor(SC.LEFT);
        IDriveDirection leftMotorRotationDirection = DriveDirection.get(leftMotor.getProperty(SC.MOTOR_REVERSE));
        DriveDirection driveDirection = (DriveDirection) curveAction.getDirection();
        if ( leftMotorRotationDirection != DriveDirection.FOREWARD ) {
            driveDirection = getDriveDirection(curveAction.getDirection() == DriveDirection.FOREWARD);
        }
        JSONObject o = mk(C.CURVE_ACTION).put(C.DRIVE_DIRECTION, driveDirection).put(C.NAME, "ev3");
        if ( duration != null ) {
            app(o);
            return app(mk(C.STOP_DRIVE).put(C.NAME, "ev3"));
        } else {
            return app(o);
        }
    }

    @Override
    public V visitMotorDriveStopAction(MotorDriveStopAction<V> stopAction) {
        JSONObject o = mk(C.STOP_DRIVE).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitMotorOnAction(MotorOnAction<V> motorOnAction) {
        boolean isDuration = motorOnAction.getParam().getDuration() != null;
        motorOnAction.getParam().getSpeed().visit(this);
        String port = motorOnAction.getUserDefinedPort();
        JSONObject o = mk(C.MOTOR_ON_ACTION).put(C.PORT, port.toLowerCase()).put(C.NAME, port.toLowerCase());
        if ( isDuration ) {
            String durationType = motorOnAction.getParam().getDuration().getType().toString().toLowerCase();
            motorOnAction.getParam().getDuration().getValue().visit(this);
            o.put(C.MOTOR_DURATION, durationType);
            app(o);
            return app(mk(C.MOTOR_STOP).put(C.PORT, port.toLowerCase()));
        } else {
            app(mk(C.EXPR).put(C.EXPR, C.NUM_CONST).put(C.VALUE, 0));
            return app(o);
        }
    }

    @Override
    public V visitMotorSetPowerAction(MotorSetPowerAction<V> motorSetPowerAction) {
        String port = motorSetPowerAction.getUserDefinedPort();
        motorSetPowerAction.getPower().visit(this);
        JSONObject o = mk(C.MOTOR_SET_POWER).put(C.PORT, port.toLowerCase());
        return app(o);
    }

    @Override
    public V visitMotorStopAction(MotorStopAction<V> motorStopAction) {
        String port = motorStopAction.getUserDefinedPort();
        JSONObject o = mk(C.MOTOR_STOP).put(C.PORT, port.toLowerCase());
        return app(o);
    }

    @Override
    public V visitShowTextAction(ShowTextAction<V> showTextAction) {
        showTextAction.getY().visit(this);
        showTextAction.getX().visit(this);
        showTextAction.getMsg().visit(this);
        JSONObject o = mk(C.SHOW_TEXT_ACTION).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitLightAction(LightAction<V> lightAction) {
        String mode = lightAction.getMode().toString().toLowerCase();
        String color = lightAction.getColor().toString().toLowerCase();
        JSONObject o = mk(C.LIGHT_ACTION).put(C.MODE, mode).put(C.COLOR, color);
        return app(o);
    }

    @Override
    public V visitTouchSensor(TouchSensor<V> touchSensor) {
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.TOUCH).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitColorSensor(ColorSensor<V> colorSensor) {
        String mode = colorSensor.getMode();
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.COLOR).put(C.MODE, mode.toLowerCase()).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitEncoderSensor(EncoderSensor<V> encoderSensor) {
        String mode = encoderSensor.getMode().toLowerCase();
        String port = encoderSensor.getPort().toLowerCase();
        JSONObject o;
        if ( mode.equals(C.RESET) ) {
            o = mk(C.ENCODER_SENSOR_RESET).put(C.PORT, port).put(C.NAME, "ev3");
        } else {
            o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.ENCODER_SENSOR_SAMPLE).put(C.PORT, port).put(C.MODE, mode).put(C.NAME, "ev3");
        }
        return app(o);
    }

    @Override
    public V visitTemperatureSensor(TemperatureSensor<V> temperatureSensor) {
        String mode = temperatureSensor.getMode();
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.TEMPERATURE).put(C.PORT, mode.toLowerCase()).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitKeysSensor(KeysSensor<V> keysSensor) {
        String mode = keysSensor.getPort().toLowerCase();
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.BUTTONS).put(C.MODE, mode).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitLightSensor(LightSensor<V> lightSensor) {
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.LIGHT).put(C.PORT, C.AMBIENTLIGHT).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitTimerSensor(TimerSensor<V> timerSensor) {
        String port = timerSensor.getPort();
        JSONObject o;
        if ( timerSensor.getMode().equals(SC.DEFAULT) || timerSensor.getMode().equals(SC.VALUE) ) {
            o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.TIMER).put(C.PORT, port).put(C.NAME, "ev3");
        } else {
            o = mk(C.TIMER_SENSOR_RESET).put(C.PORT, port).put(C.NAME, "ev3");
        }
        return app(o);
    }

    @Override
    public V visitPinTouchSensor(PinTouchSensor<V> sensorGetSample) {
        String port = sensorGetSample.getPort();
        String mode = sensorGetSample.getMode();

        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.PIN + port).put(C.MODE, mode.toLowerCase()).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitSoundSensor(SoundSensor<V> soundSensor) {
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.SOUND).put(C.MODE, C.VOLUME).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitCompassSensor(CompassSensor<V> compassSensor) {
        String mode = compassSensor.getMode();
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.COMPASS).put(C.MODE, mode.toLowerCase()).put(C.NAME, "ev3");
        return app(o);
    }

    @Override
    public V visitGyroSensor(GyroSensor<V> gyroSensor) {
        String mode = gyroSensor.getMode().toLowerCase();
        String port = gyroSensor.getPort().toLowerCase();
        JSONObject o;
        if ( mode.equals(C.RESET) ) {
            o = mk(C.GYRO_SENSOR_RESET).put(C.PORT, port).put(C.NAME, "ev3");
        } else {
            o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.GYRO).put(C.MODE, mode).put(C.NAME, "ev3");
        }
        return app(o);
    }

    @Override
    public V visitAccelerometer(AccelerometerSensor<V> accelerometerSensor) {
        return null;
    }

    @Override
    public V visitAssertStmt(AssertStmt<V> assertStmt) {
        return null;
    }

    @Override
    public V visitDebugAction(DebugAction<V> debugAction) {
        return null;
    }

    @Override
    public V visitHumiditySensor(HumiditySensor<V> humiditySensor) {
        return null;
    }

    @Override
    public V visitInfraredSensor(InfraredSensor<V> infraredSensor) {
        return null;
    }

    @Override
    public V visitUltrasonicSensor(UltrasonicSensor<V> ultrasonicSensor) {
        String mode = ultrasonicSensor.getMode();
        JSONObject o = mk(C.GET_SAMPLE).put(C.GET_SAMPLE, C.ULTRASONIC).put(C.MODE, mode.toLowerCase()).put(C.NAME, "ev3");
        return app(o);
    }

    private String getLanguageString(ILanguage language) {
        switch ( (Language) language ) {
            case GERMAN:
                return "de-DE";
            case ENGLISH:
                return "en-US";
            case FRENCH:
                return "fr-FR";
            case SPANISH:
                return "es-ES";
            case ITALIAN:
                return "it-IT";
            case DUTCH:
                return "nl-NL";
            case POLISH:
                return "pl-PL";
            case RUSSIAN:
                return "ru-RU";
            case PORTUGUESE:
                return "pt-BR";
            case JAPANESE:
                return "ja-JP";
            case CHINESE:
                return "zh-CN";
            default:
                return "";
        }
    }

    @Override
    protected void appendDuration(MotorDuration<V> duration) {
        if ( duration != null ) {
            duration.getValue().visit(this);
        } else {
            app(mk(C.EXPR).put(C.EXPR, C.NUM_CONST).put(C.VALUE, 0));
        }
    }

}
