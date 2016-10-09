package comp3110.crossingDomain;
import comp3110.mechanismsDomain.*;



/**
 * Created by u6070546 on 15/09/16.
 */
//Translating Classes Part 1 : Slide 19
public class TrafficLight extends ActiveClass implements TimerConstants{
    protected static final int SET_FAIL_MODE_EVENT = 0;

    protected static final int SET_TRAFFIC_MODE_EVENT = 1;
    private static final int DRIVE_TIME_OVER_EVENT = 2;
    protected static final int CROSSING_REQUEST_EVENT = 3;
    private static final int YELLOW_TIME_OVER_EVENT = 4;
    private static final int START_CROSSING_SEQUENCE_EVENT = 5;
    protected static final int CROSSING_CLEAR_EVENT = 6;
    private static final int SET_NEW_FAIL_MODE_EVENT = 7;
    private static final int RESET_NEW_FAIL_MODE_EVENT = 8;

    private long driveTime=8l;


    //Translating Attributes Part 1: Slide 21

    public class NewFailModeState extends State{

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight NewFailModeState procedure called");
            //hardwareEE.setYellow();
            hardwareEE.setYellow();
            ApplicationEventQueue.generateDelayedEvent(500*milliseconds,RESET_NEW_FAIL_MODE_EVENT, null, TrafficLight.this);
        }
    }
    // Translating Associations Part 1 : Slide 23
    private PedestrianLight linkedPedestrianLight;

    public void linkToPedestrianLight(PedestrianLight thePedestrianLight) {
        linkedPedestrianLight = thePedestrianLight;
    }

    public class GreenState extends State{

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight GreenState procedure called");
            hardwareEE.setGreen();
            ApplicationEventQueue.generateDelayedEvent(driveTime*seconds, DRIVE_TIME_OVER_EVENT, null, TrafficLight.this);
        }
    }

    public class FailModeState extends State{

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight FailModeState procedure called");
            //hardwareEE.processFailEvent();

            hardwareEE.setNone();
            //Messages.debug("Flash" + );
            ApplicationEventQueue.generateDelayedEvent(500*milliseconds,SET_NEW_FAIL_MODE_EVENT, null,TrafficLight.this);
        }
    }

    public class WaitingForCrossingRequestState extends State{

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight WaitingForCrossingRequestState procedure called");

        }
    }

    public class RequestPendingState extends State {

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight RequestPendingState procedure called");
        }
    }

    public class YellowState extends State{

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight YellowState procedure called");
            hardwareEE.setYellow();
            ApplicationEventQueue.generateDelayedEvent(5000*ms, YELLOW_TIME_OVER_EVENT, null, TrafficLight.this);
        }
    }

    public class RedState extends State{

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight RedState procedure called");
            hardwareEE.setRed();
            ApplicationEventQueue.generateDelayedEvent(2000*ms, START_CROSSING_SEQUENCE_EVENT, null, TrafficLight.this);
        }
    }

    public class TrafficStoppedState extends State{

        public void procedure(Object parameters) {
            Messages.debug("TrafficLight TrafficStoppedState procedure called");
            ApplicationEventQueue.generateEvent(PedestrianLight.ALLOW_CROSSING_EVENT, null, linkedPedestrianLight);
        }
    }

    public static CrossingToHardwareServerInterface hardwareEE;

    public TrafficLight(CrossingToHardwareServerInterface hardwareEE) {

        this.hardwareEE = hardwareEE;

        GreenState greenState = new GreenState();
        FailModeState failModeState = new FailModeState();
        WaitingForCrossingRequestState waitingForCrossingRequestState = new WaitingForCrossingRequestState();
        RequestPendingState  requestPendingState = new RequestPendingState();
        YellowState yellowState = new YellowState();
        RedState redState = new RedState();
        TrafficStoppedState trafficStoppedState = new TrafficStoppedState();
        NewFailModeState newFailModeState = new NewFailModeState();

        addState(greenState);
        addState(failModeState);
        addState(waitingForCrossingRequestState);
        addState(requestPendingState);
        addState(yellowState);
        addState(redState);
        addState(trafficStoppedState);
        addState(newFailModeState);

        addTransition(new Transition(greenState,  failModeState, SET_FAIL_MODE_EVENT));
        addTransition(new Transition(failModeState, greenState,  SET_TRAFFIC_MODE_EVENT));
        addTransition(new Transition(greenState, waitingForCrossingRequestState,  DRIVE_TIME_OVER_EVENT));
        addTransition(new Transition(waitingForCrossingRequestState, yellowState, CROSSING_REQUEST_EVENT));
        addTransition(new Transition(yellowState, redState, YELLOW_TIME_OVER_EVENT));
        addTransition(new Transition(redState, trafficStoppedState, START_CROSSING_SEQUENCE_EVENT));
        addTransition(new Transition(trafficStoppedState, greenState, CROSSING_CLEAR_EVENT ));
        addTransition(new Transition(greenState, requestPendingState, CROSSING_REQUEST_EVENT));
        addTransition(new Transition(requestPendingState, yellowState, DRIVE_TIME_OVER_EVENT));
        addTransition(new Transition(failModeState, newFailModeState, SET_NEW_FAIL_MODE_EVENT));
        addTransition(new Transition(newFailModeState, failModeState, RESET_NEW_FAIL_MODE_EVENT));
        setInitialState(greenState);
    }



}
