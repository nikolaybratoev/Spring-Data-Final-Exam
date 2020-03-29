package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.CarSeedDto;
import softuni.exam.models.entities.Car;
import softuni.exam.repository.CarRepository;
import softuni.exam.service.CarService;
import softuni.exam.util.ValidationUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static softuni.exam.constants.Constants.CARS_FILE_PATH;

@Service
public class CarServiceImpl implements CarService {

    private final ModelMapper modelMapper;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final CarRepository carRepository;

    @Autowired
    public CarServiceImpl(ModelMapper modelMapper,
                          Gson gson,
                          ValidationUtil validationUtil,
                          CarRepository carRepository) {
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.carRepository = carRepository;
    }

    @Override
    public boolean areImported() {
        return this.carRepository.count() > 0;
    }

    @Override
    public String readCarsFileContent() throws IOException {
        return Files.readString(Path.of(CARS_FILE_PATH));
    }

    @Override
    public String importCars() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        CarSeedDto[] carSeedDtos = this.gson
                .fromJson(new FileReader(CARS_FILE_PATH), CarSeedDto[].class);

        for (CarSeedDto carSeedDto : carSeedDtos) {
            if (this.validationUtil.isValid(carSeedDto)) {
                if (this.carRepository
                        .findFirstByMakeAndModelAndKilometers(carSeedDto.getMake(), carSeedDto.getModel(), carSeedDto.getKilometers()) == null) {
                    Car car = new Car();
                    car.setMake(carSeedDto.getMake());
                    car.setModel(carSeedDto.getModel());
                    car.setKilometers(carSeedDto.getKilometers());
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    LocalDate localDate = LocalDate.parse(carSeedDto.getRegisteredOn(), dateTimeFormatter);
                    car.setRegisteredOn(localDate);
                    stringBuilder.append(String
                            .format("Successfully imported car - %s - %s",
                                    car.getMake(),
                                    car.getModel()));
                    this.carRepository.saveAndFlush(car);
                } else {
                    stringBuilder.append("Invalid car");
                }
            } else {
                stringBuilder.append("Invalid car");
            }

            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString().trim();
    }

    @Override
    public String getCarsOrderByPicturesCountThenByMake() {
        return null;
    }

    @Override
    public Car getCarById(Long id) {
        return this.carRepository.findFirstById(id);
    }
}
