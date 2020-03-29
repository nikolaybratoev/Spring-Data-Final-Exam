package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.PictureSeedDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Picture;
import softuni.exam.repository.PictureRepository;
import softuni.exam.service.CarService;
import softuni.exam.service.PictureService;
import softuni.exam.util.ValidationUtil;

import javax.transaction.Transactional;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static softuni.exam.constants.Constants.CARS_FILE_PATH;
import static softuni.exam.constants.Constants.PICTURES_FILE_PATH;

@Service
public class PictureServiceImpl implements PictureService {

    private final CarService carService;
    private final PictureRepository pictureRepository;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    public PictureServiceImpl(CarService carService,
                              PictureRepository pictureRepository,
                              Gson gson,
                              ValidationUtil validationUtil,
                              ModelMapper modelMapper) {
        this.carService = carService;
        this.pictureRepository = pictureRepository;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count() > 0;
    }

    @Override
    public String readPicturesFromFile() throws IOException {
        return Files.readString(Path.of(PICTURES_FILE_PATH));
    }

    @Override
    @Transactional
    public String importPictures() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        PictureSeedDto[] pictureSeedDtos = this.gson
                .fromJson(new FileReader(PICTURES_FILE_PATH), PictureSeedDto[].class);

        for (PictureSeedDto pictureSeedDto : pictureSeedDtos) {
            if (this.validationUtil.isValid(pictureSeedDto)) {
                if (this.pictureRepository.findFirstByName(pictureSeedDto.getName()) == null) {
                    Picture picture = new Picture();
                    picture.setName(pictureSeedDto.getName());
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                            .ofPattern("yyyy-MM-dd' 'HH:mm:ss");
                    LocalDateTime localDateTime = LocalDateTime.
                            parse(pictureSeedDto.getDateAndTime(), dateTimeFormatter);
                    picture.setDateAndTime(localDateTime);
                    Car car = this.carService.getCarById(pictureSeedDto.getCar());
                    if (car != null) {
                        picture.setCar(car);
                        stringBuilder.append(String
                                .format("Successfully import picture - %s",
                                        picture.getName()));
                        this.pictureRepository.saveAndFlush(picture);
                    } else {
                        stringBuilder.append("Invalid picture");
                    }
                } else {
                    stringBuilder.append("Invalid picture");
                }
            } else {
                stringBuilder.append("Invalid picture");
            }
            stringBuilder.append(System.lineSeparator());
        }
        return stringBuilder.toString().trim();
    }
}
