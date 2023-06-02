# Movie Booking Application

The service will perform the tasks of ceating a new customer profile based on the details provided by the customer and dbooking tickets for the customers. it also provides admin functionality for database and ticket status manipulation.

## Description

1. The user can login/Register in the movie booking application

2. The user can view all the recent movies opened for booking. User can also search for any particular movies as well

3. bookTicket on the Movie Service will be called by passing the given information, meaning that the user can book tickets for a movie.

4. The admin can: 

   a- View booked tickets

   b- Update the balance tickets available for a movie.

## Available endpoints

1. POST: /api/v1.0/moviebooking/register (Input: Customer | Output: MessageResponse)

2. GET: /api/v1.0/ moviebooking /login Login (Input: LoginRequest | Output: AuthToken)

3. GET: /api/v1.0/ moviebooking /<username>/ (Input: username | Output: AuthToken)

4. GET: /api/v1.0/ moviebooking /all (Input: '' | Output: List<Movie>)

5. GET: /api/v/1.0/moviebooking/movies/search/moviename* (Input: username | Output: AuthToken)

6. POST: /api/v1.0/ moviebooking /<moviename>/ (Input: movie | Output: Ticket)

7. PUT: /api/v1.0/moviebooking /<moviename>/update/<ticket> (Input: moviename, ticket | Output: Movie)

8. DELETE: /api/v1.0/ moviebooking /<moviename>/delete/<id> (Input: moviename, id | Output: MessageResponse)

## Api documentation

1. Swagger UI: http://localhost:8088/swagger-ui/index.html#/
2. Swagger docs: http://localhost:8088/v3/api-docs
3. Swagger docs Json: /moviebookingapp/target/moviebookingappApiDocumentation.json

