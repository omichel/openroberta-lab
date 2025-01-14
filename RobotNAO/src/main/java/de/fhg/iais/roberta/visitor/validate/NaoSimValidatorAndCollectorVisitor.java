package de.fhg.iais.roberta.visitor.validate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ClassToInstanceMap;

import de.fhg.iais.roberta.bean.IProjectBean;
import de.fhg.iais.roberta.bean.UsedMethodBean;
import de.fhg.iais.roberta.components.ConfigurationAst;
import de.fhg.iais.roberta.mode.action.nao.Move;
import de.fhg.iais.roberta.syntax.action.nao.Animation;
import de.fhg.iais.roberta.syntax.action.nao.ApplyPosture;
import de.fhg.iais.roberta.syntax.action.nao.Autonomous;
import de.fhg.iais.roberta.syntax.action.nao.ForgetFace;
import de.fhg.iais.roberta.syntax.action.nao.GetLanguage;
import de.fhg.iais.roberta.syntax.action.nao.GetVolume;
import de.fhg.iais.roberta.syntax.action.nao.Hand;
import de.fhg.iais.roberta.syntax.action.nao.LearnFace;
import de.fhg.iais.roberta.syntax.action.nao.LedOff;
import de.fhg.iais.roberta.syntax.action.nao.LedReset;
import de.fhg.iais.roberta.syntax.action.nao.MoveJoint;
import de.fhg.iais.roberta.syntax.action.nao.PlayFile;
import de.fhg.iais.roberta.syntax.action.nao.PointLookAt;
import de.fhg.iais.roberta.syntax.action.nao.RandomEyesDuration;
import de.fhg.iais.roberta.syntax.action.nao.RastaDuration;
import de.fhg.iais.roberta.syntax.action.nao.RecordVideo;
import de.fhg.iais.roberta.syntax.action.nao.SetIntensity;
import de.fhg.iais.roberta.syntax.action.nao.SetLeds;
import de.fhg.iais.roberta.syntax.action.nao.SetMode;
import de.fhg.iais.roberta.syntax.action.nao.SetStiffness;
import de.fhg.iais.roberta.syntax.action.nao.SetVolume;
import de.fhg.iais.roberta.syntax.action.nao.Stop;
import de.fhg.iais.roberta.syntax.action.nao.TakePicture;
import de.fhg.iais.roberta.syntax.action.nao.TurnDegrees;
import de.fhg.iais.roberta.syntax.action.nao.WalkAsync;
import de.fhg.iais.roberta.syntax.action.nao.WalkDistance;
import de.fhg.iais.roberta.syntax.action.nao.WalkTo;
import de.fhg.iais.roberta.syntax.action.speech.SayTextAction;
import de.fhg.iais.roberta.syntax.action.speech.SetLanguageAction;
import de.fhg.iais.roberta.syntax.lang.functions.MathCastCharFunct;
import de.fhg.iais.roberta.syntax.lang.functions.MathCastStringFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextCharCastNumberFunct;
import de.fhg.iais.roberta.syntax.lang.functions.TextStringCastNumberFunct;
import de.fhg.iais.roberta.syntax.sensor.generic.AccelerometerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.GyroSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TimerSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.TouchSensor;
import de.fhg.iais.roberta.syntax.sensor.generic.UltrasonicSensor;
import de.fhg.iais.roberta.syntax.sensor.nao.DetectFaceSensor;
import de.fhg.iais.roberta.syntax.sensor.nao.DetectMarkSensor;
import de.fhg.iais.roberta.syntax.sensor.nao.DetectedFaceInformation;
import de.fhg.iais.roberta.syntax.sensor.nao.ElectricCurrentSensor;
import de.fhg.iais.roberta.syntax.sensor.nao.FsrSensor;
import de.fhg.iais.roberta.syntax.sensor.nao.NaoMarkInformation;
import de.fhg.iais.roberta.syntax.sensor.nao.RecognizeWord;
import de.fhg.iais.roberta.visitor.collect.NaoSimMethods;
import de.fhg.iais.roberta.visitor.hardware.INaoVisitor;

public class NaoSimValidatorAndCollectorVisitor extends CommonNepoValidatorAndCollectorVisitor implements INaoVisitor<Void> {

    public static final List<String> VALID_TOUCH_SENSOR_PORTS = Collections.singletonList("BUMPER");
    public static final List<String> VALID_TOUCH_SENSOR_SLOTS = Arrays.asList("LEFT", "RIGHT");

    public NaoSimValidatorAndCollectorVisitor(ConfigurationAst robotConfiguration, ClassToInstanceMap<IProjectBean.IBuilder<?>> beanBuilders) {
        super(robotConfiguration, beanBuilders);
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.WAIT_TIME);
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.RESET_POSE);
    }

    @Override
    public Void visitTouchSensor(TouchSensor<Void> touchSensor) {
        boolean isPortValid = VALID_TOUCH_SENSOR_PORTS.stream().anyMatch(str -> str.equalsIgnoreCase(touchSensor.getUserDefinedPort()));
        boolean isSlotValid = VALID_TOUCH_SENSOR_SLOTS.stream().anyMatch(str -> str.equalsIgnoreCase(touchSensor.getSlot()));

        if ( !isPortValid || !isSlotValid ) {
            addErrorToPhrase(touchSensor, "SIM_BLOCK_NOT_SUPPORTED");
        }
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.IS_TOUCHED);
        return null;
    }

    @Override
    public Void visitUltrasonicSensor(UltrasonicSensor<Void> ultrasonicSensor) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_ULTRASONIC);
        return null;
    }

    @Override
    public Void visitGyroSensor(GyroSensor<Void> gyroSensor) {
        switch ( gyroSensor.getSlot().toUpperCase() ) {
            case "X":
                this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_GYRO_X);
                break;
            case "Y":
                this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_GYRO_Y);
                break;
        }
        return null;
    }

    @Override
    public Void visitAccelerometerSensor(AccelerometerSensor<Void> accelerometerSensor) {
        switch ( accelerometerSensor.getUserDefinedPort().toUpperCase() ) {
            case "X":
                this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_ACCELEROMETER_X);
                break;
            case "Y":
                this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_ACCELEROMETER_Y);
                break;
            case "Z":
                this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_ACCELEROMETER_Z);
                break;
        }
        return null;
    }

    @Override
    public Void visitSetIntensity(SetIntensity<Void> setIntensity) {
        requiredComponentVisited(setIntensity, setIntensity.getIntensity());
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_LED);
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_INTENSITY);
        return null;
    }

    @Override
    public Void visitFsrSensor(FsrSensor<Void> forceSensor) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_FORCE);
        return null;
    }

    @Override
    public Void visitHand(Hand<Void> hand) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.MOVE_HAND_JOINT);
        return null;
    }

    @Override
    public Void visitMoveJoint(MoveJoint<Void> moveJoint) {
        requiredComponentVisited(moveJoint, moveJoint.getDegrees());
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.MOVE_JOINT);
        return null;
    }

    @Override
    public Void visitWalkDistance(WalkDistance<Void> walkDistance) {
        requiredComponentVisited(walkDistance, walkDistance.getDistanceToWalk());
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.WALK_DISTANCE);
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.CALC_DIST);
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_FORCE);
        return null;
    }

    @Override
    public Void visitTurnDegrees(TurnDegrees<Void> turnDegrees) {
        requiredComponentVisited(turnDegrees, turnDegrees.getDegreesToTurn());
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.TURN);
        return null;
    }

    @Override
    public Void visitSetLeds(SetLeds<Void> setLeds) {
        requiredComponentVisited(setLeds, setLeds.getColor());
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_LED);
        return null;
    }

    @Override
    public Void visitLedOff(LedOff<Void> ledOff) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_LED);
        return null;
    }

    @Override
    public Void visitLedReset(LedReset<Void> ledReset) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.LED_OFF);
        return null;
    }

    @Override
    public Void visitWalkAsync(WalkAsync<Void> walkAsync) {
        requiredComponentVisited(walkAsync, walkAsync.getXSpeed(), walkAsync.getYSpeed(), walkAsync.getZSpeed());
        addWarningToPhrase(walkAsync, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitSetMode(SetMode<Void> setMode) {
        addWarningToPhrase(setMode, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitApplyPosture(ApplyPosture<Void> applyPosture) {
        switch ( applyPosture.getPosture() ) {
            case SITRELAX:
            case SIT:
            case LYINGBELLY:
            case LYINGBACK:
                addWarningToPhrase(applyPosture, "SIM_BLOCK_NOT_SUPPORTED");
                break;
            default:
                this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.ANIMATION);
                this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_LED);
                break;
        }
        return null;
    }

    @Override
    public Void visitSetStiffness(SetStiffness<Void> setStiffness) {
        addWarningToPhrase(setStiffness, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitAutonomous(Autonomous<Void> autonomous) {
        addWarningToPhrase(autonomous, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitWalkTo(WalkTo<Void> walkTo) {
        requiredComponentVisited(walkTo, walkTo.getWalkToX(), walkTo.getWalkToY(), walkTo.getWalkToTheta());
        addWarningToPhrase(walkTo, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitStop(Stop<Void> stop) {
        addWarningToPhrase(stop, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitAnimation(Animation<Void> animation) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.ANIMATION);
        if ( animation.getMove() == Move.BLINK ) {
            this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_LED);
        }
        return null;
    }

    @Override
    public Void visitPointLookAt(PointLookAt<Void> pointLookAt) {
        requiredComponentVisited(pointLookAt, pointLookAt.getPointX(), pointLookAt.getPointY(), pointLookAt.getPointZ(), pointLookAt.getSpeed());
        addWarningToPhrase(pointLookAt, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitSetVolume(SetVolume<Void> setVolume) {
        requiredComponentVisited(setVolume, setVolume.getVolume());
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_VOLUME);
        return null;
    }

    @Override
    public Void visitGetVolume(GetVolume<Void> getVolume) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_VOLUME);
        return null;
    }

    @Override
    public Void visitSetLanguageAction(SetLanguageAction<Void> setLanguageAction) {
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SET_LANGUAGE);
        return null;
    }

    @Override
    public Void visitGetLanguage(GetLanguage<Void> getLanguage) {
        addErrorToPhrase(getLanguage, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitSayTextAction(SayTextAction<Void> sayTextAction) {
        requiredComponentVisited(sayTextAction, sayTextAction.getMsg());
        optionalComponentVisited(sayTextAction.getPitch());
        optionalComponentVisited(sayTextAction.getSpeed());
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.SAY_TEXT);
        return null;
    }

    @Override
    public Void visitPlayFile(PlayFile<Void> playFile) {
        requiredComponentVisited(playFile, playFile.getMsg());
        addWarningToPhrase(playFile, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitRandomEyesDuration(RandomEyesDuration<Void> randomEyesDuration) {
        requiredComponentVisited(randomEyesDuration, randomEyesDuration.getDuration());
        addWarningToPhrase(randomEyesDuration, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitRastaDuration(RastaDuration<Void> rastaDuration) {
        requiredComponentVisited(rastaDuration, rastaDuration.getDuration());
        addWarningToPhrase(rastaDuration, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitDetectMarkSensor(DetectMarkSensor<Void> detectedMark) {
        addWarningToPhrase(detectedMark, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitTakePicture(TakePicture<Void> takePicture) {
        requiredComponentVisited(takePicture, takePicture.getPictureName());
        addWarningToPhrase(takePicture, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitRecordVideo(RecordVideo<Void> recordVideo) {
        requiredComponentVisited(recordVideo, recordVideo.getVideoName(), recordVideo.getDuration());
        addWarningToPhrase(recordVideo, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitLearnFace(LearnFace<Void> learnFace) {
        requiredComponentVisited(learnFace, learnFace.getFaceName());
        addErrorToPhrase(learnFace, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitForgetFace(ForgetFace<Void> forgetFace) {
        requiredComponentVisited(forgetFace, forgetFace.getFaceName());
        addWarningToPhrase(forgetFace, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitDetectFaceSensor(DetectFaceSensor<Void> detectFace) {
        addErrorToPhrase(detectFace, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitElectricCurrentSensor(ElectricCurrentSensor<Void> electricCurrent) {
        addErrorToPhrase(electricCurrent, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitRecognizeWord(RecognizeWord<Void> recognizeWord) {
        requiredComponentVisited(recognizeWord, recognizeWord.vocabulary);
        this.getBuilder(UsedMethodBean.Builder.class).addUsedMethod(NaoSimMethods.GET_RECOGNIZED_WORD);
        return null;
    }

    @Override
    public Void visitNaoMarkInformation(NaoMarkInformation<Void> naoMarkInformation) {
        requiredComponentVisited(naoMarkInformation, naoMarkInformation.getNaoMarkId());
        addErrorToPhrase(naoMarkInformation, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitDetectedFaceInformation(DetectedFaceInformation<Void> detectedFaceInformation) {
        requiredComponentVisited(detectedFaceInformation, detectedFaceInformation.getFaceName());
        addErrorToPhrase(detectedFaceInformation, "SIM_BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitTimerSensor(TimerSensor<Void> timerSensor) {
        addWarningToPhrase(timerSensor, "BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitMathCastStringFunct(MathCastStringFunct<Void> mathCastStringFunct) {
        addWarningToPhrase(mathCastStringFunct, "BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitMathCastCharFunct(MathCastCharFunct<Void> mathCastCharFunct) {
        addWarningToPhrase(mathCastCharFunct, "BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitTextStringCastNumberFunct(TextStringCastNumberFunct<Void> textStringCastNumberFunct) {
        addWarningToPhrase(textStringCastNumberFunct, "BLOCK_NOT_SUPPORTED");
        return null;
    }

    @Override
    public Void visitTextCharCastNumberFunct(TextCharCastNumberFunct<Void> textCharCastNumberFunct) {
        addWarningToPhrase(textCharCastNumberFunct, "BLOCK_NOT_SUPPORTED");
        return null;
    }
}
