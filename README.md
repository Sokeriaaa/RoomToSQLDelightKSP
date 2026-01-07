# RoomToSQLDelightKSP
This is an experimental project for generating [SQLDelight](https://github.com/sqldelight/sqldelight) code using the annotations in the [Room](https://developer.android.com/jetpack/androidx/releases/room) with [KSP](https://github.com/google/ksp).

The project is introduced when I am developing [*return 0;*](https://github.com/Sokeriaaa/Return0) — an RPG game powered by Compose Multiplatform. Both Room and SQLDelight are used to implement the database for this game, with SQLDelight for the JS, Room for the non-JS platforms. I reused the data classes and Dao interfaces in commonMain and implemented them manually in the webMain. To improve efficiency, this project was born.

## What does this project do

Currently, this project can generate *.sq files based on annotations from Room.

### Supported annotations for now

- [x] Entity
  - [x] ColumnInfo
  - [x] PrimaryKey
  - [x] Index
  - [ ] Embedded
  - [ ] Relation
- [x] Dao
  - [x] Insert
  - [x] Query
  - [ ] Update
  - [ ] Upsert
  - [ ] Delete
- [ ] Database
- [ ] AutoMigration
