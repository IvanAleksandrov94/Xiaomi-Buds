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
            btHelper.sendMessage0()
        }
        Button("ШУМОДАВ"){
            btHelper.sendMessage1()
        }
        Button("Прозрачность"){
            btHelper.sendMessage2()
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




class BluetoothHelper: NSObject {
    
    private var name: String!
    
    private var comDevice: IOBluetoothDevice?
    private var comChannel: IOBluetoothRFCOMMChannel!
    
    
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
            //            let sppServiceUUID = IOBluetoothSDPUUID.uuid32(kBluetoothSDPUUID16ServiceClassSerialPort.rawValue)
            let serialPortServiceRecode = comDevice.getServiceRecord(for: uuid)
            
            var rfcommChannelIDD: BluetoothRFCOMMChannelID = 0;
            let result =  serialPortServiceRecode?.getRFCOMMChannelID(&rfcommChannelIDD)
            if(result == kIOReturnSuccess){
                print("!!!!!!СУЩЕСТВУЕТ!!!!!")
                print("Bluetooth Helper: Found Channel")
            }
            comDevice.requestAuthentication()
            
            
            
            let isSuccess = comDevice.openRFCOMMChannelAsync(&channel, withChannelID: rfcommChannelIDD, delegate: self)
            if (isSuccess == kIOReturnSuccess) {
                print("Bluetooth Helper: Started to Open Channel")
                comChannel = channel!
            } else {
                print("ОШИБКА ПОДКЛЮЧЕНИЯ")
            }
        }
    }
    
    func sendMessage0() {
        // print("Bluetooth Helper: Sending Message (\(string))")
        var bytes: [UInt8] = [0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x07, 0x02, 0x04, 0x00, 0xef]
        
        print(comChannel.isOpen())
        
        
        if(comChannel?.writeSync(&bytes, length:12) == kIOReturnSuccess){
            print("Успешно отправлено")
        }else{
            print("Не удалось отправить")
        }
    }
    
    func sendMessage1() {
        // print("Bluetooth Helper: Sending Message (\(string))")
        var bytes: [UInt8] = [0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x05, 0x02, 0x04, 0x01, 0xef]
        
        print(comChannel.isOpen())
        if(comChannel?.writeSync(&bytes, length:12) == kIOReturnSuccess){
            print("Успешно отправлено")
        }else{
            print("Не удалось отправить")
        }
    }
    
    func sendMessage2() {
        var bytes: [UInt8] = [0xfe, 0xdc, 0xba, 0xc1, 0x08, 0x00, 0x04, 0x06, 0x02, 0x04, 0x02, 0xef]
        print(comChannel.isOpen())
        if(comChannel?.writeSync(&bytes,length: 12) == kIOReturnSuccess){
            print("Успешно отправлено")
        }else{
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
    
    //    func rfcommChannelData(rfcommChannel: IOBluetoothRFCOMMChannel!, data dataPointer: UnsafeMutablePointer<Void>, length dataLength: Int) {
    //            if let dataString = NSString(data: NSData(bytes: dataPointer, length: dataLength), encoding: NSUTF8StringEncoding) as? String {
    //                delegate?.bluetoothHelperReceivedString(dataString)
    //            }
    //        }
    
//    func rfcommChannelData(_ rfcommChannel: IOBluetoothRFCOMMChannel!,
//                           data dataPointer: UnsafeMutableRawPointer!,
//                           length dataLength: Int) {
//        //        print("rfcommChannelData")
//        let array = Array(UnsafeBufferPointer(start: dataPointer.assumingMemoryBound(to: Int8.self), count: dataLength))
//        print(array)
//    }
}

