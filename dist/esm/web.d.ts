import { ListenerCallback, PluginListenerHandle, WebPlugin } from '@capacitor/core';
import type { AllowedResult, GoogleFitPlugin } from './definitions';
export declare class GoogleFitWeb extends WebPlugin implements GoogleFitPlugin {
    constructor();
    connectToGoogleFit(): Promise<void>;
    addListener(eventName: 'googleFitAllowed', listenerFunc?: ListenerCallback): Promise<PluginListenerHandle>;
    disableFit(): Promise<void>;
    logoutGoogleFit(): Promise<void>;
    openGoogleFit(): Promise<void>;
    isAllowed(): Promise<AllowedResult>;
    isPermissionGranted(): Promise<AllowedResult>;
    setWriteSleepData(): Promise<{
        value: string;
    }>;
    settingSleepSegment(): Promise<{
        value: string;
    }>;
    writeStepCountData(): Promise<{
        value: string;
    }>;
    writeSleepSegmentData(): Promise<{
        value: string;
    }>;
    readSleepData(): Promise<any>;
    isGoogleFitInstalled(): Promise<{
        value: boolean;
    }>;
    getHistory(): Promise<any>;
    getHistoryActivity(): Promise<any>;
    getHistoryActivityPerDay(): Promise<any>;
}
