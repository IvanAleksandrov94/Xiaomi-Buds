//
//  StocksListViewModel.swift
//  BudsController
//
//  Created by Ivan on 06.07.2022.
//

import Foundation


@MainActor
class PeopleListViewModel: ObservableObject {
    
    private let webService = WebApi()
    private let baseUrl = Const.Urls.baseUrl
    
    
    @Published var people: PeopleViewModel?
    
    func fetchPeople () async {
        do {
            
         let people = try await webService.getPeople(url: baseUrl!)
            self.people = PeopleViewModel.init(people: people)
        } catch {
            print(error)
        }
    }
}


struct PeopleViewModel {
    
    private var people: People
    
    init(people: People){
        self.people = people
    }
    var userId :Int? {
        people.userId
    }
    var id :Int? {
        people.id
    }
    var title :String? {
        people.title
    }
    var completed :Bool? {
        people.completed
    }
    
}
