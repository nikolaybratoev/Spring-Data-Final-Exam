package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import softuni.exam.models.dtos.SellerSeedDto;
import softuni.exam.models.dtos.SellerSeedRootDto;
import softuni.exam.models.entities.Seller;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static softuni.exam.constants.Constants.PICTURES_FILE_PATH;
import static softuni.exam.constants.Constants.SELLERS_FILE_PATH;

@Service
public class SellerServiceImpl implements SellerService {

    private final ModelMapper modelMapper;
    private final SellerRepository sellerRepository;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;

    public SellerServiceImpl(ModelMapper modelMapper,
                             SellerRepository sellerRepository,
                             XmlParser xmlParser,
                             ValidationUtil validationUtil) {
        this.modelMapper = modelMapper;
        this.sellerRepository = sellerRepository;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
    }

    @Override
    public boolean areImported() {
        return this.sellerRepository.count() > 0;
    }

    @Override
    public String readSellersFromFile() throws IOException {
        return Files.readString(Path.of(SELLERS_FILE_PATH));
    }

    @Override
    public String importSellers() throws IOException, JAXBException {
        StringBuilder stringBuilder = new StringBuilder();

        SellerSeedRootDto sellerSeedRootDto = this.xmlParser
                .unmarshalFromFile(SELLERS_FILE_PATH, SellerSeedRootDto.class);

        for (SellerSeedDto sellerSeedDto : sellerSeedRootDto.getSellerSeedDtos()) {
            if (this.validationUtil.isValid(sellerSeedDto)) {
                if (this.sellerRepository
                        .findFirstByFirstNameAndLastNameAndRatingAndEmail(
                                sellerSeedDto.getFirstName(),
                                sellerSeedDto.getLastName(),
                                sellerSeedDto.getRating(),
                                sellerSeedDto.getEmail()) == null) {
                    Seller seller = this.modelMapper
                            .map(sellerSeedDto, Seller.class);

                    this.sellerRepository.saveAndFlush(seller);
                    stringBuilder.append(String
                            .format("Successfully import seller %s - %s",
                                    seller.getLastName(),
                                    seller.getEmail()));
                } else {
                    stringBuilder.append("Invalid seller");
                }
            } else {
                stringBuilder.append("Invalid seller");
            }
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString().trim();
    }

    @Override
    public Seller getSellerById(Long id) {
        return this.sellerRepository.findFirstById(id);
    }
}
