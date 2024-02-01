import {
  ListenerCallback,
  PluginListenerHandle,
  WebPlugin,
} from '@capacitor/core';

import type { AllowedResult, GoogleFitPlugin } from './definitions';

export class GoogleFitWeb extends WebPlugin implements GoogleFitPlugin {
  constructor() {
    super({
      name: 'GoogleFit',
      platforms: ['web'],
    });
  }

  async connectToGoogleFit(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  addListener(
    eventName: 'googleFitAllowed',
    listenerFunc?: ListenerCallback,
  ): Promise<PluginListenerHandle> & PluginListenerHandle {
    throw new Error(`Method not implemented.${eventName}${listenerFunc}`);
  }
  async disableFit(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  async logoutGoogleFit(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  async openGoogleFit(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  async isAllowed(): Promise<AllowedResult> {
    throw new Error('Method not implemented.');
  }
  async isPermissionGranted(): Promise<AllowedResult> {
    throw new Error('Method not implemented.');
  }
  async setWriteSleepData(): Promise<{ value: string }> {
    throw new Error('Method not implemented.');
  }
  async writeStepCountData(): Promise<{ value: string }> {
    throw new Error('Method not implemented.');
  }
  async writeSleepSegmentData(): Promise<{ value: string }> {
    throw new Error('Method not implemented.');
  }
  async readSleepData(): Promise<any> {
    throw new Error('Method not implemented.');
  }
  async isGoogleFitInstalled(): Promise<{ value: boolean }> {
    throw new Error('Method not implemented.');
  }
  async getHistory(): Promise<any> {
    throw new Error('Method not implemented.');
  }
  async getHistoryActivity(): Promise<any> {
    throw new Error('Method not implemented.');
  }
  async getHistoryActivityPerDay(): Promise<any> {
    throw new Error('Method not implemented.');
  }
}
