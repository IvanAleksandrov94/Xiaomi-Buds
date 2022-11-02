//
//  ContentView.swift
//  BudsController
//
//  Created by Ivan on 06.07.2022.
//

import SwiftUI

import Cocoa
import IOBluetooth
import IOBluetoothUI
import Foundation
import AVFoundation

struct ContentView: View {
    
    @StateObject private var vm: PeopleListViewModel
    
    private var targetDeviceName = "Xiaomi Buds 3T Pro";
    
    var btHelper:BluetoothHelper!
    
    var btDevice:IOBluetoothDevice!
    
    init(vm: PeopleListViewModel) {
        btHelper = BluetoothHelper(deviceName: targetDeviceName)
        self._vm = StateObject(wrappedValue: vm)
        var deviceMapping: [String:String] = [:]
        IOBluetoothDevice.pairedDevices().forEach({(device) in
            guard let device = device as? IOBluetoothDevice,
                  let deviceName = device.name,
                  let addressString = device.addressString
            else { return }
            
            deviceMapping[deviceName] = addressString;
        })
        let targetDevice = deviceMapping[targetDeviceName] ?? "unknown";
        
        guard let bluetoothDevice = IOBluetoothDevice(addressString: targetDevice) else {
            print("Error reconnecting \(targetDevice)");
            exit(1)
        }
        btDevice = bluetoothDevice
        btHelper.deviceInquiryDeviceFound(device: btDevice)
        btHelper.deviceInquiryComplete()
        
    }
    
    var body: some View {
        Button("Отключить"){
            btHelper.sendOff()
        }
        Button("ШУМОДАВ"){
            btHelper.sendNoice()
        }
        Button("Прозрачность"){
            btHelper.sendTransparent()
        }
        Button("Connect") {
            btHelper.deviceInquiryDeviceFound(device: btDevice)
            btHelper.deviceInquiryComplete()

        }
        
        
    }
    
    struct ContentView_Previews: PreviewProvider {
        static var previews: some View {
            Group {
                ContentView(vm: PeopleListViewModel())
            }
        }
    }
    
}




class BluetoothHelper: NSObject ,IOBluetoothRFCOMMChannelDelegate{
    
    private var name: String!
    
    private var comDevice: IOBluetoothDevice?
    private var comChannel: IOBluetoothRFCOMMChannel!
    private var audioEnvironment: AVAudioEnvironmentNode = AVAudioEnvironmentNode()
    private var engine: AVAudioEngine = AVAudioEngine()
    private let player = AVAudioPlayerNode()

    
    
    init(deviceName: String) {
        super.init()
        name = deviceName
        IOBluetoothDeviceInquiry(delegate: self).start()
        
        
        
    }
    
    func deviceInquiryComplete() {
        if let comDevice = comDevice {
            print("Bluetooth Helper: Found Correct Device (\(String(describing: comDevice.name)))")
            
            var channel: IOBluetoothRFCOMMChannel? = IOBluetoothRFCOMMChannel()
            let uuid = IOBluetoothSDPUUID(uuid16: 0xfd2d)
            let serialPortServiceRecode = comDevice.getServiceRecord(for: uuid)
            var rfcommChannelIDD: BluetoothRFCOMMChannelID = 0;
            let result =  serialPortServiceRecode?.getRFCOMMChannelID(&rfcommChannelIDD)
            
            if(result == kIOReturnSuccess){
                print("Bluetooth Helper: Found Channel")
            }
            
            comDevice.requestAuthentication()
            
            let isSuccess = comDevice.openRFCOMMChannelAsync(&channel, withChannelID: rfcommChannelIDD, delegate: self)
            
            if (isSuccess == kIOReturnSuccess) {
                setUpAudioEngine()
                print("Bluetooth Helper: Started to Open Channel")
                comChannel = channel!
                
            } else {
                print("Bluetooth Helper: Connection Error")
            }
            
        }
    }
    
    private func setUpAudioEngine() {
        let x = Float(Int.random(in: -360..<360))
        let y = Float(Int.random(in: -360..<360))
        let z = Float(Int.random(in: -360..<360))
        let yaw = Float(Int.random(in: -360..<360))
        let pitch  = Float(Int.random(in: -360..<360))
        let roll = Float(Int.random(in: -360..<360))
        
        print("///////")
        print(x, y, z)
        print(yaw, pitch, roll)
        print("///////")
        audioEnvironment.listenerPosition = AVAudio3DPoint(x: x, y: y, z: z)
        audioEnvironment.listenerAngularOrientation = AVAudio3DAngularOrientation(yaw: yaw, pitch: pitch, roll: roll)
        audioEnvironment.position = AVAudio3DPoint(x: x, y: y, z: z)
        audioEnvironment.reverbParameters.enable = true
        audioEnvironment.reverbBlend = 1
        audioEnvironment.reverbParameters.level = -34
        audioEnvironment.reverbParameters.loadFactoryReverbPreset(.mediumChamber)
       
//
        var outputDeviceID: AudioDeviceID = 56
//
        do {
            try engine.outputNode.auAudioUnit.setDeviceID(outputDeviceID)
         }  catch {
           print(error)
         }


        let result:OSStatus = AudioUnitSetProperty(engine.outputNode.audioUnit!, kAudioOutputUnitProperty_CurrentDevice, kAudioUnitScope_Global, 0, &outputDeviceID, UInt32(MemoryLayout<AudioObjectPropertyAddress>.size))
        if result != 0  {
           print("error setting output device \(result)")
           return
        }
        
     

        engine.attach(audioEnvironment)
        engine.attach(player)
        engine.connect(player, to: engine.mainMixerNode, format: nil)

        engine.connect(audioEnvironment, to: engine.outputNode, format: engine.outputNode.outputFormat(forBus: 0))
        engine.prepare()

        do {
            try engine.start()

            print("Playing!")
            audioEnvironment.position = AVAudio3DPoint(x: x, y: y, z: z)
            audioEnvironment.listenerPosition = AVAudio3DPoint(x: x, y: y, z: z)
            audioEnvironment.listenerAngularOrientation = AVAudio3DAngularOrientation(yaw: yaw, pitch: pitch, roll: roll)
        } catch {
            print("Couldn't start engine due to error:", error.localizedDescription)
        }
        
   
        
        engine.mainMixerNode.installTap(onBus: 0, bufferSize: 1024, format: nil) { // I've tried with a format and without
                (buffer: AVAudioPCMBuffer?, time: AVAudioTime!) -> Void in
                do {
//                    self.engine.disconnectNodeOutput(self.player)
//                    self.engine.connect(self.player, to: self.engine.mainMixerNode, format: buffer!.format)
                   //
                    self.player.scheduleBuffer(buffer!)
                    self.player.play()
                    
                    
                    
                } catch _{
                    print("Problem Writing Buffer")
                }
            }
    }
    
    // receive data
    func rfcommChannelData(_ rfcommChannel: IOBluetoothRFCOMMChannel!, data dataPointer: UnsafeMutableRawPointer!, length dataLength: Int) {
        let array = Array(UnsafeBufferPointer(start: dataPointer.assumingMemoryBound(to: Int8.self), count: dataLength))
        //print(array)
        //audioEnvironment.position = AVAudio3DPoint(x: 0, y: -360, z: 0)
        audioEnvironment.volume = 1
       // audioEnvironment.listenerPosition = AVAudio3DPoint(x: 0, y: -360, z: 0)
       // audioEnvironment.listenerAngularOrientation = AVAudioMake3DAngularOrientation(0.0, 360, -360)
        print(audioEnvironment.engine?.isRunning)
        
    }
    
    func sendOff() {
        var bytes: [UInt8] = [0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07, 0x02, 0x04, 0x00, 0xef]
        
        if(comChannel?.writeSync(&bytes, length:12) == kIOReturnSuccess){
            print("Успешно отправлено")
        } else{
            print("Не удалось отправить")
        }
    }
    
    func sendNoice() {
        var bytes: [UInt8] = [0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05, 0x02, 0x04, 0x01, 0xef]
        
        if(comChannel?.writeSync(&bytes, length:12) == kIOReturnSuccess){
            print("Успешно отправлено")
        } else{
            print("Не удалось отправить")
        }
    }
    
    func sendTransparent() {
        var bytes: [UInt8] = [0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x06, 0x02, 0x04, 0x02, 0xef]

        if(comChannel?.writeSync(&bytes,length: 12) == kIOReturnSuccess){
            print("Успешно отправлено")
        } else{
            print("Не удалось отправить")
        }
    }

    
    
    func deviceInquiryDeviceFound( device: IOBluetoothDevice!) {
        if device.name == name {
            comDevice = device
        }
        else {
            print("Bluetooth Helper: Found Other Device (\(String(describing: device.name)): \(String(describing: device.addressString)))")
        }
    }
}

