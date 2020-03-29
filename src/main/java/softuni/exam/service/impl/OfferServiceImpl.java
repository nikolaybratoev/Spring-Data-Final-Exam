package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.OfferSeedDto;
import softuni.exam.models.dtos.OfferSeedRootDto;
import softuni.exam.models.entities.Car;
import softuni.exam.models.entities.Offer;
import softuni.exam.models.entities.Seller;
import softuni.exam.repository.OfferRepository;
import softuni.exam.service.CarService;
import softuni.exam.service.OfferService;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static softuni.exam.constants.Constants.OFFERS_FILE_PATH;

@Service
public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;
    private final CarService carService;
    private final SellerService sellerService;

    public OfferServiceImpl(OfferRepository offerRepository,
                            ModelMapper modelMapper,
                            XmlParser xmlParser,
                            ValidationUtil validationUtil,
                            CarService carService,
                            SellerService sellerService) {
        this.offerRepository = offerRepository;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
        this.carService = carService;
        this.sellerService = sellerService;
    }

    @Override
    public boolean areImported() {
        return this.offerRepository.count() > 0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return Files.readString(Path.of(OFFERS_FILE_PATH));
    }

    @Override
    @Transactional
    public String importOffers() throws IOException, JAXBException {
        StringBuilder stringBuilder = new StringBuilder();

        OfferSeedRootDto offerSeedRootDto = this.xmlParser
                .unmarshalFromFile(OFFERS_FILE_PATH, OfferSeedRootDto.class);

        for (OfferSeedDto offerSeedDto : offerSeedRootDto.getOfferSeedDtos()) {
            if (this.validationUtil.isValid(offerSeedDto)) {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                        .ofPattern("yyyy-MM-dd' 'HH:mm:ss");
                LocalDateTime localDateTime = LocalDateTime
                        .parse(offerSeedDto.getAddedOn(), dateTimeFormatter);
                if (this.offerRepository
                        .findFirstByDescriptionAndAddedOn(offerSeedDto.getDescription(), localDateTime) == null) {
                    Car car = this.carService.getCarById(offerSeedDto
                            .getCarDto().getId());

                    Seller seller = this.sellerService.getSellerById(offerSeedDto
                            .getSellerDto().getId());

                    if (seller != null && car != null){
                        Offer offer = this.modelMapper.map(offerSeedDto, Offer.class);
                        offer.setAddedOn(localDateTime);
                        offer.setCar(car);
                        offer.setSeller(seller);
                        this.offerRepository.saveAndFlush(offer);
                        String stringData = offer.getAddedOn()
                                .format(DateTimeFormatter
                                        .ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                        stringBuilder.append(String
                                .format("Successfully import offer %s - %s",
                                stringData,
                                offer.isHasGoldStatus()));
                    }else {
                        stringBuilder.append("Invalid offer");
                    }
                } else {
                    stringBuilder.append("Invalid offer");
                }
            } else {
                stringBuilder.append("Invalid offer");
            }
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString().trim();
    }
}
