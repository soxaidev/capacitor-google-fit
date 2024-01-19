import { WebPlugin, } from '@capacitor/core';
export class GoogleFitWeb extends WebPlugin {
    constructor() {
        super({
            name: 'GoogleFit',
            platforms: ['web'],
        });
    }
    async connectToGoogleFit() {
        throw new Error('Method not implemented.');
    }
    addListener(eventName, listenerFunc) {
        throw new Error(`Method not implemented.${eventName}${listenerFunc}`);
    }
    async disableFit() {
        throw new Error('Method not implemented.');
    }
    async logoutGoogleFit() {
        throw new Error('Method not implemented.');
    }
    async openGoogleFit() {
        throw new Error('Method not implemented.');
    }
    async isAllowed() {
        throw new Error('Method not implemented.');
    }
    async isPermissionGranted() {
        throw new Error('Method not implemented.');
    }
    async setWriteSleepData() {
        throw new Error('Method not implemented.');
    }
    async settingSleepSegment() {
        throw new Error('Method not implemented.');
    }
    async writeStepCountData() {
        throw new Error('Method not implemented.');
    }
    async writeSleepSegmentData() {
        throw new Error('Method not implemented.');
    }
    async readSleepData() {
        throw new Error('Method not implemented.');
    }
    async isGoogleFitInstalled() {
        throw new Error('Method not implemented.');
    }
    async getHistory() {
        throw new Error('Method not implemented.');
    }
    async getHistoryActivity() {
        throw new Error('Method not implemented.');
    }
    async getHistoryActivityPerDay() {
        throw new Error('Method not implemented.');
    }
}
//# sourceMappingURL=web.js.map