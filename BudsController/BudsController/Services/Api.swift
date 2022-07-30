//
//  Api.swift
//  BudsController
//
//  Created by Ivan on 06.07.2022.
//

import Foundation
import SwiftUI


enum NetworkError: Error {
    case invalidResponse
}


class WebApi {
    func getPeople(url: URL) async throws -> People {
        let (data, response) = try await URLSession.shared.data(from: url)
        guard let httpResponse  = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            throw NetworkError.invalidResponse
        }
        return try JSONDecoder().decode(People.self, from: data)
    }
}
