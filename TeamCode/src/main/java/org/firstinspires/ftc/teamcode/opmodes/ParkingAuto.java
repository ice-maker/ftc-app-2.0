package org.firstinspires.ftc.teamcode.opmodes;

import static org.opencv.core.CvType.CV_8UC4;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;

import com.arcrobotics.ftclib.geometry.Pose2d;
import com.arcrobotics.ftclib.geometry.Rotation2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.hardware.Drivetrain;
import org.firstinspires.ftc.teamcode.hardware.Lift;
import org.firstinspires.ftc.teamcode.hardware.navigation.OdometryNav;
import org.firstinspires.ftc.teamcode.hardware.navigation.PID;
import org.firstinspires.ftc.teamcode.hardware.Robot;
import org.firstinspires.ftc.teamcode.util.Logger;
import org.firstinspires.ftc.teamcode.util.LoopTimer;
import org.firstinspires.ftc.teamcode.vision.AprilTagDetectionPipeline;
import org.firstinspires.ftc.teamcode.vision.ConeInfoDetector;
import org.firstinspires.ftc.teamcode.vision.webcam.Webcam;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.openftc.apriltag.AprilTagDetection;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import java.util.ArrayList;

@Autonomous(name = "Parking Auto")
public class ParkingAuto extends LoggingOpMode{

    private Drivetrain drivetrain;
    private OdometryNav odometry;

    private String result = "Nothing";

    private int main_id = 0;

    private OpenCvCamera camera;
    private AprilTagDetectionPipeline aprilTagDetectionPipeline;

    private static final double FEET_PER_METER = 3.28084;

    private double fx = 578.272;
    private double fy = 578.272;
    private double cx = 402.145;
    private double cy = 221.506;

    private double tagsize = 0.166;

    private final Logger log = new Logger("Parking Auto");

    @Override
    public void init() {
        super.init();
        Robot robot = Robot.initialize(hardwareMap);
        drivetrain = robot.drivetrain;


        Pose2d start_pose = new Pose2d(0,0,new Rotation2d(Math.toRadians(45.0)));
        odometry.updatePose(start_pose);

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"), cameraMonitorViewId);
        aprilTagDetectionPipeline = new AprilTagDetectionPipeline(tagsize, fx, fy, cx, cy);

        camera.setPipeline(aprilTagDetectionPipeline);
        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                camera.startStreaming(800,448, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode)
            {

            }
        });

        telemetry.setMsTransmissionInterval(50);

    }

    @Override
    public void init_loop() {
        super.init_loop();

        ArrayList<AprilTagDetection> currentDetections = aprilTagDetectionPipeline.getLatestDetections();

        if(currentDetections.size() != 0) {

            for (AprilTagDetection tag : currentDetections) {
                if (tag.id == 107) {
                    result = "FTC8813: 1";
                    break;
                }
                else if (tag.id == 350) {
                    result = "FTC8813: 2";
                    break;
                }
                else if (tag.id == 25) {
                    result = "FTC8813: 3";
                    break;
                }
                else {
                    result = "Nothing";
                }

            }
        }


        telemetry.addData("Detected", result);

        telemetry.update();
    }

    @Override
    public void start() {
        super.start();
        drivetrain.resetEncoders();
    }

    @Override
    public void loop() {

        odometry.updatePose();

        switch (main_id) {
            case 0:
                drivetrain.autoMove(24,0,0,0,1,1,10, odometry.getPose(),telemetry);
                if (drivetrain.hasReached()) {
                    main_id += 1;
                }
                break;
            case 1:
                switch (result) {
                    case "FTC8813: 1":
                        drivetrain.autoMove(26,-24,0,0,1,1,10, odometry.getPose(),telemetry);
                        if (drivetrain.hasReached()) {
                            main_id += 1;
                        }
                        break;
                    case "FTC8813: 3":
                        drivetrain.autoMove(26,24,0,0,1,1,10, odometry.getPose(),telemetry);
                        if (drivetrain.hasReached()) {
                            main_id += 1;
                        }
                        break;
                    default:
                        main_id += 1;
                        break;
                }
                break;
            case 2:
                drivetrain.stop();
                break;
        }

        telemetry.addData("Loop Time: ", LoopTimer.getLoopTime());
        telemetry.update();

        LoopTimer.resetTimer();

    }

    @Override
    public void stop() {
        super.stop();
    }

}
