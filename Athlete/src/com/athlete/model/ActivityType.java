package com.athlete.model;

import android.util.Log;

import com.athlete.R;
import com.athlete.exception.InvalidActivitySubTypeException;
import com.athlete.exception.InvalidActivityTypeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by santiago on 02/11/13.
 */
public class ActivityType{
    private String name;
    private String charRepresentation;
    private List<ActivitySubType> subtypes;

    private ActivityType(String name, String charRepresentation, List<ActivitySubType> subtypes){
        this.name= name;
        this.charRepresentation = charRepresentation;
        this.subtypes = subtypes;
    }

    public String getName() {
        return name;
    }

    public String getCharRepresentation() {
        return charRepresentation;
    }

    public List<ActivitySubType> getSubtypes() {
        return subtypes;
    }

    @Override
    public String toString() {
        return getName();
    }

    public ActivitySubType getSubtype(String subtypeName) throws InvalidActivitySubTypeException {
        for(ActivitySubType subtype: subtypes) {
            if(subtype.getName().equalsIgnoreCase(subtypeName))
                return subtype;
        }
        throw new InvalidActivitySubTypeException("Invalid Subtype");
    }

    public boolean hasSubtype(String subtypeName){
        for(ActivitySubType subtype: subtypes) {
            if(subtype.getName().equalsIgnoreCase(subtypeName))
                return true;
        }
        return false;
    }

    public String getDefaultHexColor() {
        return "#FFF";
    }

    public static ActivityType getDefaultActivityType() {
        return ActivityType.RUNNING;
    }
    /* Static definitions */

    /**
        Activity types and Subtypes reference. This class is useful
        as a reference mainly. To define the String constants and to
        have a quick and easy visual representation.
    */
    public static class ACTIVITY_TYPES {
        /* RUNNING */
        public static final String RUNNING = "Running";
        public class RUNNING_SUBTYPES {
            public static final String ENDURANCE = "Endurance";
            public static final String TEMPO = "Tempo";
            public static final String SLOW = "Slow";
            public static final String INTERVAL = "Interval";
            public static final String GROUP = "Group";
            public static final String ELEVATION = "Elevation";
            public static final String RACE = "Race";
        }

        /* WALKING */
        public static final String WALKING = "Walking";
        public class WALKING_SUBTYPES {
            public static final String LEISURE = "Leisure";
            public static final String MODERATE = "Moderate";
            public static final String INTENSE = "Intense";
            public static final String DOG_WALKING = "Dog Walking";
            public static final String SPEED_WALKING = "Speed Walking";
        }

        /* CYCLING */
        public static final String CYCLING = "Cycling";
        public class CYCLING_SUBTYPES {
            public static final String ENDURANCE = "Endurance";
            public static final String RECOVERY = "Recovery";
            public static final String TEMPO = "Tempo";
            public static final String INTERVAL = "Interval";
            public static final String CLIMBING = "Climbing";
            public static final String RACE = "Race";
            public static final String GROUP = "Group";
            public static final String LEISURE = "Leisure";
            public static final String SPIN = "Spin";
        }

        /* HIKING */
        public static final String HIKING = "Hiking";
        public class HIKING_SUBTYPES {
            public static final String CROSS_COUNTRY = "Cross Country";
            public static final String LEISURE = "Leisure";
            public static final String BACKPACKING = "Backpacking";
            public static final String INTENSE = "Intense";
            public static final String ALPINE = "Alpine";
            public static final String RUCK = "Ruck";
        }

        /* MOUNTAIN_BIKING */
        public static final String MOUNTAIN_BIKING = "Mountain Biking";
        public class MOUNTAIN_BIKING_SUBTYPES {
            public static final String CROSS_COUNTRY = "Cross Country";
            public static final String DOWNHILL = "Downhill";
            public static final String CLIMBING = "Climbing";
            public static final String RACE = "Race";
            public static final String LEISURE = "Leisure";
        }

        /* WHEELCHAIR */
        public static final String WHEELCHAIR = "Wheelchair";
        public class WHEELCHAIR_SUBTYPES {
            public static final String ENDURANCE = "Endurance";
            public static final String TEMPO = "Tempo";
            public static final String INTERVAL = "Interval";
            public static final String RACE = "Race";
            public static final String LEISURE = "Leisure";
        }

        /* PADDLING */
        public static final String PADDLING = "Paddling";
        public class PADDLING_SUBTYPES {
            public static final String KAYAKING = "Kayaking";
            public static final String CANOEING = "Canoeing";
            public static final String SUP = "SUP";
            public static final String RAFTING = "Rafting";
        }

        /* X_COUNTRY_SKIING */
        public static final String X_COUNTRY_SKIING = "X-Country Skiing";
        public class X_COUNTRY_SKIING_SUBTYPES {
            public static final String CROSS_COUNTRY = "Cross Country";
            public static final String DOWNHILL = "Downhill";
            public static final String SKATE = "Skate";
            public static final String SKATE_V_GRANNY = "Skate V (Granny)";
            public static final String SKATE_V1_OFFSET = "Skate V1 (Offset)";
            public static final String SKATE_V2_WASSBERG = "Skate V2 (Wassberg)";
            public static final String V2A_MOGREN = "V2a (Mogren)";
            public static final String RACE = "Race";
        }

        /* DOWNHILL_SKIING */
        public static final String DOWNHILL_SKIING = "Downhill Skiing";
        public class DOWNHILL_SKIING_SUBTYPES {
            public static final String ALL_MOUNTAIN = "All Mountain";
            public static final String BACKCOUNTRY = "Backcountry";
            public static final String TELEMARK = "Telemark";
            public static final String RANDONEE = "Randonee";
            public static final String SLALOM = "Slalom";
            public static final String GS = "GS";
            public static final String DOWNHILL = "Downhill";
            public static final String BUMPS = "Bumps";
            public static final String PARK = "Park";
            public static final String RACE = "Race";
        }

        /* SNOWBOARDING */
        public static final String SNOWBOARDING = "Snowboarding";
        public class SNOWBOARDING_SUBTYPES {
            public static final String ALL_MOUNTAIN = "All Mountain";
            public static final String BACKCOUNTRY = "Backcountry";
            public static final String SLALOM = "Slalom";
            public static final String GS = "GS";
            public static final String BUMPS = "Bumps";
            public static final String PARK = "Park";
            public static final String RACE = "Race";
        }

        /* SWIMMING */
        public static final String SWIMMING = "Swimming";
        public class SWIMMING_SUBTYPES {
            public static final String POOL = "Pool";
            public static final String OPEN_WATER = "Open Water";
            public static final String FREESTYLE = "Freestyle";
            public static final String BREAST = "Breast";
            public static final String BACK = "Back";
            public static final String IM = "IM";
            public static final String FLY = "Fly";
            public static final String KICK = "Kick";
            public static final String KICK_W_FINS = "Kick w/ Fins";
            public static final String RACE = "Race";
        }

    }

    /**
     * Activity subtype class.
     */
    public static class ActivitySubType {
        private String name;
        private int colorResource;

        private ActivitySubType(String name, int colorResource){
            this.name = name;
            this.colorResource = colorResource;
        }

        public String getName() {
            return name;
        }

        public int getColorResource() {
            return colorResource;
        }
    }

    private static HashMap<String, ActivityType> activityTypeMapping;

    /* Predefined Activity Types */
    private static final ActivityType RUNNING = new ActivityType(
        ACTIVITY_TYPES.RUNNING, "1",
        new ArrayList<ActivitySubType>(Arrays.asList(
                new ActivitySubType(ACTIVITY_TYPES.RUNNING_SUBTYPES.ENDURANCE, R.color.all_mountain),
                new ActivitySubType(ACTIVITY_TYPES.RUNNING_SUBTYPES.TEMPO, R.color.tempo),
                new ActivitySubType(ACTIVITY_TYPES.RUNNING_SUBTYPES.SLOW, R.color.slow),
                new ActivitySubType(ACTIVITY_TYPES.RUNNING_SUBTYPES.INTERVAL, R.color.interval),
                new ActivitySubType(ACTIVITY_TYPES.RUNNING_SUBTYPES.GROUP, R.color.group),
                new ActivitySubType(ACTIVITY_TYPES.RUNNING_SUBTYPES.ELEVATION, R.color.elevation),
                new ActivitySubType(ACTIVITY_TYPES.RUNNING_SUBTYPES.RACE, R.color.race)
        ))
    );
    private static final ActivityType WALKING = new ActivityType(
        ACTIVITY_TYPES.WALKING, "2",
        new ArrayList<ActivitySubType>(Arrays.asList(
                new ActivitySubType(ACTIVITY_TYPES.WALKING_SUBTYPES.LEISURE, R.color.leisure),
                new ActivitySubType(ACTIVITY_TYPES.WALKING_SUBTYPES.MODERATE, R.color.moderate),
                new ActivitySubType(ACTIVITY_TYPES.WALKING_SUBTYPES.INTENSE, R.color.intense),
                new ActivitySubType(ACTIVITY_TYPES.WALKING_SUBTYPES.DOG_WALKING, R.color.dog_walking),
                new ActivitySubType(ACTIVITY_TYPES.WALKING_SUBTYPES.SPEED_WALKING, R.color.speed_walking)
        ))
    );

    private static final ActivityType CYCLING = new ActivityType(
            ACTIVITY_TYPES.CYCLING, "3",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.ENDURANCE, R.color.endurance),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.RECOVERY, R.color.recovery),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.TEMPO, R.color.tempo),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.INTERVAL, R.color.interval),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.CLIMBING, R.color.climbing),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.RACE, R.color.race),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.GROUP, R.color.group),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.LEISURE, R.color.leisure),
                    new ActivitySubType(ACTIVITY_TYPES.CYCLING_SUBTYPES.SPIN, R.color.spin)
            ))
    );

    private static final ActivityType HIKING = new ActivityType(
            ACTIVITY_TYPES.HIKING, "4",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.HIKING_SUBTYPES.CROSS_COUNTRY, R.color.cross_country),
                    new ActivitySubType(ACTIVITY_TYPES.HIKING_SUBTYPES.LEISURE, R.color.leisure),
                    new ActivitySubType(ACTIVITY_TYPES.HIKING_SUBTYPES.BACKPACKING, R.color.backpacking),
                    new ActivitySubType(ACTIVITY_TYPES.HIKING_SUBTYPES.INTENSE, R.color.intense),
                    new ActivitySubType(ACTIVITY_TYPES.HIKING_SUBTYPES.ALPINE, R.color.alpine),
                    new ActivitySubType(ACTIVITY_TYPES.HIKING_SUBTYPES.RUCK, R.color.ruck)
            ))
    );

    private static final ActivityType MOUNTAIN_BIKING = new ActivityType(
            ACTIVITY_TYPES.MOUNTAIN_BIKING, "5",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.MOUNTAIN_BIKING_SUBTYPES.CROSS_COUNTRY, R.color.cross_country),
                    new ActivitySubType(ACTIVITY_TYPES.MOUNTAIN_BIKING_SUBTYPES.DOWNHILL, R.color.downhill),
                    new ActivitySubType(ACTIVITY_TYPES.MOUNTAIN_BIKING_SUBTYPES.CLIMBING, R.color.climbing),
                    new ActivitySubType(ACTIVITY_TYPES.MOUNTAIN_BIKING_SUBTYPES.RACE, R.color.race),
                    new ActivitySubType(ACTIVITY_TYPES.MOUNTAIN_BIKING_SUBTYPES.LEISURE, R.color.leisure)
            ))
    );

    private static final ActivityType WHEELCHAIR = new ActivityType(
            ACTIVITY_TYPES.WHEELCHAIR, "6",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.WHEELCHAIR_SUBTYPES.ENDURANCE, R.color.endurance),
                    new ActivitySubType(ACTIVITY_TYPES.WHEELCHAIR_SUBTYPES.TEMPO, R.color.tempo),
                    new ActivitySubType(ACTIVITY_TYPES.WHEELCHAIR_SUBTYPES.INTERVAL, R.color.interval),
                    new ActivitySubType(ACTIVITY_TYPES.WHEELCHAIR_SUBTYPES.RACE, R.color.race),
                    new ActivitySubType(ACTIVITY_TYPES.WHEELCHAIR_SUBTYPES.LEISURE, R.color.leisure)
            ))
    );

    private static final ActivityType PADDLING = new ActivityType(
            ACTIVITY_TYPES.PADDLING, "7",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.PADDLING_SUBTYPES.KAYAKING, R.color.kayaking),
                    new ActivitySubType(ACTIVITY_TYPES.PADDLING_SUBTYPES.CANOEING, R.color.canoeing),
                    new ActivitySubType(ACTIVITY_TYPES.PADDLING_SUBTYPES.SUP, R.color.sup),
                    new ActivitySubType(ACTIVITY_TYPES.PADDLING_SUBTYPES.RAFTING, R.color.rafting)
            ))
    );

    private static final ActivityType X_COUNTRY_SKIING = new ActivityType(
            ACTIVITY_TYPES.X_COUNTRY_SKIING, "8",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.CROSS_COUNTRY, R.color.cross_country),
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.DOWNHILL, R.color.downhill),
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.SKATE, R.color.skate),
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.SKATE_V_GRANNY, R.color.skate_v_granny),
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.SKATE_V1_OFFSET, R.color.skate_v1_offset),
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.SKATE_V2_WASSBERG, R.color.skate_v2_wassberg),
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.V2A_MOGREN, R.color.v2a_mogren),
                    new ActivitySubType(ACTIVITY_TYPES.X_COUNTRY_SKIING_SUBTYPES.RACE, R.color.race)
            ))
    );

    private static final ActivityType DOWNHILL_SKIING = new ActivityType(
            ACTIVITY_TYPES.DOWNHILL_SKIING, "9",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.ALL_MOUNTAIN, R.color.all_mountain),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.BACKCOUNTRY, R.color.backcountry),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.TELEMARK, R.color.telemark),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.RANDONEE, R.color.randonee),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.SLALOM, R.color.slalom),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.GS, R.color.gs),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.DOWNHILL, R.color.downhill),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.BUMPS, R.color.bumps),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.PARK, R.color.park),
                    new ActivitySubType(ACTIVITY_TYPES.DOWNHILL_SKIING_SUBTYPES.RACE, R.color.race)
            ))
    );

    private static final ActivityType SNOWBOARDING = new ActivityType(
            ACTIVITY_TYPES.SNOWBOARDING, "0",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.SNOWBOARDING_SUBTYPES.ALL_MOUNTAIN, R.color.all_mountain),
                    new ActivitySubType(ACTIVITY_TYPES.SNOWBOARDING_SUBTYPES.BACKCOUNTRY, R.color.backcountry),
                    new ActivitySubType(ACTIVITY_TYPES.SNOWBOARDING_SUBTYPES.SLALOM, R.color.slalom),
                    new ActivitySubType(ACTIVITY_TYPES.SNOWBOARDING_SUBTYPES.GS, R.color.gs),
                    new ActivitySubType(ACTIVITY_TYPES.SNOWBOARDING_SUBTYPES.BUMPS, R.color.bumps),
                    new ActivitySubType(ACTIVITY_TYPES.SNOWBOARDING_SUBTYPES.PARK, R.color.park),
                    new ActivitySubType(ACTIVITY_TYPES.SNOWBOARDING_SUBTYPES.RACE, R.color.race)
            ))
    );

    private static final ActivityType SWIMMING = new ActivityType(
            ACTIVITY_TYPES.SWIMMING, "-",
            new ArrayList<ActivitySubType>(Arrays.asList(
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.POOL, R.color.pool),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.OPEN_WATER, R.color.open_water),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.FREESTYLE, R.color.freestyle),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.BREAST, R.color.breast),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.BACK, R.color.back),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.IM, R.color.im),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.FLY, R.color.fly),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.KICK, R.color.kick),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.KICK_W_FINS, R.color.kick_w_fins),
                    new ActivitySubType(ACTIVITY_TYPES.SWIMMING_SUBTYPES.RACE, R.color.race)
            ))
    );

    static {
        activityTypeMapping = new HashMap<String, ActivityType>();

        activityTypeMapping.put(ACTIVITY_TYPES.RUNNING, RUNNING);
        activityTypeMapping.put(ACTIVITY_TYPES.WALKING, WALKING);
        activityTypeMapping.put(ACTIVITY_TYPES.CYCLING, CYCLING);
        activityTypeMapping.put(ACTIVITY_TYPES.HIKING, HIKING);
        activityTypeMapping.put(ACTIVITY_TYPES.MOUNTAIN_BIKING, MOUNTAIN_BIKING);
        activityTypeMapping.put(ACTIVITY_TYPES.WHEELCHAIR, WHEELCHAIR);
        activityTypeMapping.put(ACTIVITY_TYPES.PADDLING, PADDLING);
        activityTypeMapping.put(ACTIVITY_TYPES.X_COUNTRY_SKIING, X_COUNTRY_SKIING);
        activityTypeMapping.put(ACTIVITY_TYPES.DOWNHILL_SKIING, DOWNHILL_SKIING);
        activityTypeMapping.put(ACTIVITY_TYPES.SNOWBOARDING, SNOWBOARDING);
        activityTypeMapping.put(ACTIVITY_TYPES.SWIMMING, SWIMMING);
    }

    public static ActivityType getFromName(String name) throws InvalidActivityTypeException{
        Log.d("ATHLETE-FONT-ACTIVITY-TYPE", String.format("getFromName called with name: %s", name));
        if(!activityTypeMapping.containsKey(name)){
            Log.d("ATHLETE-FONT-ACTIVITY-TYPE", String.format("NAME WAS NOT FOUND!!!!!!!!", name));
            throw new InvalidActivityTypeException("Invalid Activity type");
        }
        return activityTypeMapping.get(name);
    }

    public static List<ActivityType> getRegisteredActivityTypes(){
        return Arrays.asList(
            RUNNING, WALKING, CYCLING,
            HIKING, MOUNTAIN_BIKING, WHEELCHAIR,
            PADDLING, X_COUNTRY_SKIING, DOWNHILL_SKIING,
            SNOWBOARDING, SWIMMING
        );
    }
}

