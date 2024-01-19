import type { PluginListenerHandle } from '@capacitor/core';
export interface GoogleFitPlugin {
    /**
     * Connect to Google Fit
     * @returns {Promise}
     * @resolve any
     */
    connectToGoogleFit(): Promise<void>;
    /**
     * Disable Google Fit
     */
    disableFit(): Promise<void>;
    /**
     * Logout from Google Fit
     */
    logoutGoogleFit(): Promise<void>;
    /**
     * openGoogleFit
     */
    openGoogleFit(): Promise<void>;
    /**
     * Returns wether the permissions are ok or not
     * @returns {Promise}
     * @resolve AllowedResult
     */
    isAllowed(): Promise<AllowedResult>;
    /**
     * Check if permission is granted
     */
    isPermissionGranted(): Promise<AllowedResult>;
    /**
     * Check if Google Fit is installed
     */
    isGoogleFitInstalled(): Promise<{
        value: boolean;
    }>;
    /**
     * Get history
     * @returns {Promise}
     * @resolve AccountData
     */
    getHistory(call: QueryInput): Promise<DayContainer>;
    /**
     * setWriteSleepData
     */
    setWriteSleepData(call: SetSleepData): Promise<{
        value: string;
    }>;
    /**
     * settingSleepSegment
     */
    settingSleepSegment(call: QueryInput): Promise<{
        value: string;
    }>;
    /**
     * writeStepCountData
     */
    writeStepCountData(call: SetStepCountData): Promise<{
        value: string;
    }>;
    /**
     * readSleepData
     */
    readSleepData(call: QueryInput): Promise<any>;
    /**
     * Get history activity
     * @returns {Promise}
     * @resolve AccountData
     */
    getHistoryActivity(call: QueryInput): Promise<ActivityContainer>;
    /**
     * Get history activity per day
     * @returns {Promise}
     * @resolve AccountData
     */
    getHistoryActivityPerDay(call: QueryInput): Promise<ActivityContainer>;
    addListener(eventName: 'googleFitAllowed', listenerFunc: (info: GoogleFitPermissionData) => void): Promise<PluginListenerHandle>;
}
export interface PermissionData {
    allowed: boolean;
}
export interface GoogleFitPermissionData {
    value: 'success' | 'failure';
}
export interface QueryInput {
    startTime: Date;
    endTime: Date;
}
export interface SetSleepData {
    startTime: Date;
    endTime: Date;
    id: string;
    sleepStage: number;
}
export interface SetStepCountData {
    startTime: Date;
    endTime: Date;
    value: number;
}
export interface ActivityContainer {
    activities: HistoryActivityData[];
}
export interface DayContainer {
    days: HistoryData[];
}
export interface HistoryData {
    start: string;
    end: string;
    /**
    Distance travelled in meters.
    Valid range: 0—100 meters per second
     */
    distance: string;
    /**meters per second */
    speed: string;
    calories: string;
}
export interface HistoryActivityData {
    start: string;
    end: string;
    distance?: string;
    speed?: string;
    calories?: string;
    activity?: string;
    weight?: string;
    steps?: string;
    sourceName: string;
    sourceType: string;
}
export interface AllowedResult {
    allowed: boolean;
}
